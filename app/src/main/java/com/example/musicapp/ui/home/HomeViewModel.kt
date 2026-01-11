package com.example.musicapp.ui.home

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
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
import androidx.core.net.toUri

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(val songs: List<Song>) : HomeUiState
    object PermissionRequired : HomeUiState
    object Error : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MusicRepository,
    @ApplicationContext private val context: Context) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

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

    // Hàm gọi khi bấm vào 1 bài hát
    fun playMusic(song: Song) {
        val controller = mediaController
        if (controller == null) {
            Log.e("MusicApp", "Controller chưa sẵn sàng, thử lại sau!")
            return
        }

        // Tạo gói dữ liệu bài hát (MediaItem)
        val mediaItem = MediaItem.Builder()
            .setUri(song.contentUri)
            .setMediaId(song.id.toString())
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .setArtworkUri(song.albumArtUri?.toUri())
                    .build()
            )
            .build()

        // Gửi lệnh xuống Service
        controller.setMediaItem(mediaItem)
        controller.prepare()
        controller.play()
    }

    fun onPermissionDenied() {
        _uiState.value = HomeUiState.PermissionRequired
    }

    // Ngắt kết nối khi thoát màn hình để tránh rò rỉ bộ nhớ
    override fun onCleared() {
        super.onCleared()
        mediaController?.release()
    }
}