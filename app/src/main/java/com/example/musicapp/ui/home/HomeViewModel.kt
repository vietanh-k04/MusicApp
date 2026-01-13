package com.example.musicapp.ui.home

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.musicapp.domain.model.Song
import com.example.musicapp.domain.repository.MusicRepository
import com.example.musicapp.service.MusicService
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(val songs: List<Song>) : HomeUiState
    object PermissionRequired : HomeUiState
    object Error : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MusicRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // --- CÁC BIẾN STATE (TRẠNG THÁI UI) ---

    // 1. Trạng thái tải dữ liệu (Loading/List Nhạc/Lỗi)
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // 2. Bài hát đang phát
    private val _currentPlayingSong = MutableStateFlow<Song?>(null)
    val currentPlayingSong = _currentPlayingSong.asStateFlow()

    // 3. Trạng thái Play/Pause
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    // 4. Tổng thời gian bài hát (Duration)
    private val _duration = MutableStateFlow(0L)
    val duration = _duration.asStateFlow()

    // 5. Vị trí hiện tại (Current Position - cho thanh Seekbar)
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition = _currentPosition.asStateFlow()

    // 6. Trạng thái Shuffle (Trộn bài)
    private val _shuffleMode = MutableStateFlow(false)
    val shuffleMode = _shuffleMode.asStateFlow()

    // 7. Trạng thái Repeat (Lặp lại)
    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode = _repeatMode.asStateFlow()

    // Công cụ điều khiển nhạc (Giao tiếp với Service)
    private var mediaController: MediaController? = null

    init {
        // Khởi động kết nối tới MusicService
        val sessionToken = SessionToken(context, ComponentName(context, MusicService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        controllerFuture.addListener({
            try {
                mediaController = controllerFuture.get()
                // Sau khi kết nối thành công:
                setupPlayerListener() // 1. Lắng nghe sự kiện từ Service
                updateProgress()      // 2. Bắt đầu vòng lặp cập nhật Seekbar
            } catch (e: Exception) {
                Log.e("MusicApp", "Lỗi kết nối Service: ${e.message}")
            }
        }, MoreExecutors.directExecutor())
    }

    // --- CÁC HÀM XỬ LÝ DỮ LIỆU ---

    fun loadSongs() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                val songs = repository.getLocalSongs()
                _uiState.value = HomeUiState.Success(songs)
            } catch (_: Exception) {
                _uiState.value = HomeUiState.Error
            }
        }
    }

    fun searchOnline(query: String) {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                val songs = repository.searchSongs(query)
                if (songs.isEmpty()) {
                    _uiState.value = HomeUiState.Error
                } else {
                    _uiState.value = HomeUiState.Success(songs)
                }
            } catch (_: Exception) {
                _uiState.value = HomeUiState.Error
            }
        }
    }

    fun onPermissionDenied() {
        _uiState.value = HomeUiState.PermissionRequired
    }

    // --- CÁC HÀM ĐIỀU KHIỂN NHẠC (CORE LOGIC) ---

    fun playMusic(song: Song) {
        val controller = mediaController ?: return

        // 1. Lấy danh sách nhạc hiện tại để nạp vào Playlist (Hỗ trợ Next/Prev)
        val currentList = (uiState.value as? HomeUiState.Success)?.songs ?: return

        // 2. Tìm vị trí bài hát được bấm
        val index = currentList.indexOfFirst { it.id == song.id }
        if (index == -1) return

        // Cập nhật UI ngay lập tức (cho mượt)
        _currentPlayingSong.value = song
        _isPlaying.value = true

        // 3. Nếu đang bấm đúng bài đang phát -> Chỉ toggle Play/Pause
        if (controller.currentMediaItem?.mediaId == song.id.toString()) {
            if (controller.isPlaying) controller.pause() else controller.play()
            return
        }

        // 4. Biến đổi List<Song> -> List<MediaItem> để gửi cho ExoPlayer
        val mediaItems = currentList.map { item ->
            MediaItem.Builder()
                .setUri(item.contentUri)
                .setMediaId(item.id.toString())
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(item.title)
                        .setArtist(item.artist)
                        .setArtworkUri((item.albumArtUri ?: "").toUri())
                        .build()
                )
                .build()
        }

        // 5. Nạp danh sách và nhảy tới đúng bài
        controller.setMediaItems(mediaItems)
        controller.seekTo(index, 0L) // Nhảy tới bài thứ 'index'
        controller.prepare()
        controller.play()
    }

    fun toggleMusic() {
        val controller = mediaController ?: return
        if (controller.isPlaying) controller.pause() else controller.play()
    }

    fun skipToNext() {
        val controller = mediaController ?: return
        if (controller.hasNextMediaItem()) {
            controller.seekToNextMediaItem()
        }
    }

    fun skipToPrevious() {
        val controller = mediaController ?: return
        // Nếu đã chạy quá 3 giây -> Replay lại từ đầu
        if (controller.currentPosition > 3000) {
            controller.seekTo(0)
        } else {
            // Nếu chưa quá 3 giây -> Lùi về bài trước
            if (controller.hasPreviousMediaItem()) {
                controller.seekToPreviousMediaItem()
            } else {
                controller.seekTo(0)
            }
        }
    }

    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
        _currentPosition.value = position // Cập nhật UI ngay
    }

    fun toggleShuffle() {
        val controller = mediaController ?: return
        controller.shuffleModeEnabled = !controller.shuffleModeEnabled
    }

    fun toggleRepeat() {
        val controller = mediaController ?: return
        val newMode = when (controller.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        controller.repeatMode = newMode
    }

    // --- CÁC HÀM ĐỒNG BỘ UI & SERVICE ---

    private fun setupPlayerListener() {
        val controller = mediaController ?: return

        // Đồng bộ trạng thái ban đầu
        _isPlaying.value = controller.isPlaying
        _shuffleMode.value = controller.shuffleModeEnabled
        _repeatMode.value = controller.repeatMode
        syncCurrentSong(controller)

        controller.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                syncCurrentSong(controller)
            }

            // Quan trọng: Lấy Duration chuẩn khi Player load xong
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    _duration.value = controller.duration.coerceAtLeast(0L)
                }
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                _shuffleMode.value = shuffleModeEnabled
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                _repeatMode.value = repeatMode
            }
        })
    }

    private fun syncCurrentSong(controller: MediaController) {
        val item = controller.currentMediaItem
        if (item != null) {
            _currentPlayingSong.value = Song(
                id = item.mediaId.toLongOrNull(),
                title = item.mediaMetadata.title.toString(),
                artist = item.mediaMetadata.artist.toString(),
                contentUri = item.requestMetadata.mediaUri.toString(),
                albumArtUri = item.mediaMetadata.artworkUri.toString()
            )
            // Cập nhật duration dự phòng
            if (controller.duration > 0) {
                _duration.value = controller.duration
            }
        }
    }

    private fun updateProgress() {
        viewModelScope.launch {
            while (true) {
                val controller = mediaController
                if (controller != null && _isPlaying.value) {
                    val current = controller.currentPosition
                    _currentPosition.value = current

                    // Logic dự phòng: Cập nhật duration nếu chưa có
                    if (_duration.value <= 0 && controller.duration > 0) {
                        _duration.value = controller.duration
                    }
                }
                delay(1000) // Cập nhật mỗi 1 giây
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaController?.release()
    }
}