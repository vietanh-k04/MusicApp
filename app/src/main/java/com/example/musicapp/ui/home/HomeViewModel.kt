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

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _currentPlayingSong = MutableStateFlow<Song?>(null)
    val currentPlayingSong = _currentPlayingSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    // Công cụ điều khiển nhạc (Remote Control)
    private var mediaController: MediaController? = null

    init {
        // 1. Khởi động kết nối tới MusicService
        val sessionToken = SessionToken(context, ComponentName(context, MusicService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        controllerFuture.addListener({
            try {
                mediaController = controllerFuture.get()
                Log.d("MusicApp", "Kết nối Service thành công!")

                // --- SỬA 1: GỌI HÀM LẮNG NGHE Ở ĐÂY ---
                setupPlayerListener()
                // -------------------------------------

            } catch (e: Exception) {
                Log.e("MusicApp", "Lỗi kết nối Service: ${e.message}")
            }
        }, MoreExecutors.directExecutor())
    }

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

    fun playMusic(song: Song) {
        val controller = mediaController ?: return

        // --- SỬA 2: CẬP NHẬT UI NGAY LẬP TỨC ---
        _currentPlayingSong.value = song
        _isPlaying.value = true
        // ---------------------------------------

        // Nếu đang chọn đúng bài đang phát thì chỉ toggle Play/Pause
        if (controller.currentMediaItem?.mediaId == song.id.toString()) {
            if (controller.isPlaying) controller.pause() else controller.play()
            return
        }

        // Nếu bài mới thì phát mới
        val mediaItem = MediaItem.Builder()
            .setUri(song.contentUri)
            .setMediaId(song.id.toString())
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .setArtworkUri((song.albumArtUri ?: "").toUri())
                    .build()
            )
            .build()

        controller.setMediaItem(mediaItem)
        controller.prepare()
        controller.play()
    }

    fun searchOnline(query: String) {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading // Hiện loading
            try {
                // Gọi repository
                val songs = repository.searchSongs(query)
                if (songs.isEmpty()) {
                    _uiState.value = HomeUiState.Error
                } else {
                    _uiState.value = HomeUiState.Success(songs)
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error
            }
        }
    }

    private fun setupPlayerListener() {
        val controller = mediaController ?: return

        // Cập nhật trạng thái ban đầu
        _isPlaying.value = controller.isPlaying
        syncCurrentSong(controller)

        // Đăng ký lắng nghe sự kiện
        controller.addListener(object : Player.Listener {
            // Khi trạng thái Play/Pause thay đổi
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }

            // Khi bài hát thay đổi (Next/Prev hoặc hết bài)
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                syncCurrentSong(controller)
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
        }
    }

    fun onPermissionDenied() {
        _uiState.value = HomeUiState.PermissionRequired
    }

    // Ngắt kết nối khi thoát màn hình để tránh rò rỉ bộ nhớ
    override fun onCleared() {
        super.onCleared()
        mediaController?.release()
    }

    fun toggleMusic() {
        val controller = mediaController ?: return
        if (controller.isPlaying) controller.pause() else controller.play()
    }
}