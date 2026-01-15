package com.example.musicapp.ui.viewmodel

import android.content.ComponentName
import android.content.Context
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.net.toUri
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.musicapp.domain.model.Song
import com.example.musicapp.service.MusicService
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.musicapp.data.local.DataStoreManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class SharedViewModel @Inject constructor(@ApplicationContext private val context: Context, private val dataStoreManager: DataStoreManager) : ViewModel() {
    private val _currentPlayingSong = MutableStateFlow<Song?>(null)
    val currentPlayingSong = _currentPlayingSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration = _duration.asStateFlow()

    private val _shuffleMode = MutableStateFlow(false)
    val shuffleMode = _shuffleMode.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode = _repeatMode.asStateFlow()

    private val _remainingTime = MutableStateFlow<Long?>(null)
    val remainingTime = _remainingTime.asStateFlow()

    private var mediaController: MediaController? = null
    private var timer: CountDownTimer? = null

    val isDarkTheme = dataStoreManager.isDarkThemeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    init {
        val sessionToken = SessionToken(context, ComponentName(context, MusicService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        controllerFuture.addListener({
            try {
                mediaController = controllerFuture.get()
                setupPlayerListener()
                updateProgress()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, MoreExecutors.directExecutor())
    }

    // --- LOGIC HẸN GIỜ ---
    fun setSleepTimer(minutes: Int) {
        timer?.cancel() // Hủy timer cũ nếu có

        if (minutes == 0) {
            _remainingTime.value = null
            return
        }

        val millis = minutes * 60 * 1000L

        timer = object : CountDownTimer(millis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _remainingTime.value = millisUntilFinished
            }

            override fun onFinish() {
                _remainingTime.value = null
                // Hết giờ -> Tắt nhạc
                mediaController?.pause()
            }
        }.start()
    }

    fun cancelSleepTimer() {
        timer?.cancel()
        _remainingTime.value = null
    }

    // --- PLAYER CONTROLS ---
    fun playMusic(song: Song, playlist: List<Song>) {
        val controller = mediaController ?: return

        _currentPlayingSong.value = song
        _isPlaying.value = true

        if (controller.currentMediaItem?.mediaId == song.id.toString()) {
            if (controller.isPlaying) controller.pause() else controller.play()
            return
        }

        val index = playlist.indexOfFirst { it.id == song.id }
        if (index == -1) return

        val mediaItems = playlist.map { item ->
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

        controller.setMediaItems(mediaItems)
        controller.seekTo(index, 0L)
        controller.prepare()
        controller.play()
    }

    fun toggleMusic() {
        val controller = mediaController ?: return
        if (controller.isPlaying) controller.pause() else controller.play()
    }

    fun skipToNext() {
        val controller = mediaController ?: return
        if (controller.hasNextMediaItem()) controller.seekToNextMediaItem()
    }

    fun skipToPrevious() {
        val controller = mediaController ?: return
        if (controller.currentPosition > 3000) controller.seekTo(0)
        else if (controller.hasPreviousMediaItem()) controller.seekToPreviousMediaItem()
        else controller.seekTo(0)
    }

    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
        _currentPosition.value = position
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

    private fun setupPlayerListener() {
        val controller = mediaController ?: return
        _isPlaying.value = controller.isPlaying
        _shuffleMode.value = controller.shuffleModeEnabled
        _repeatMode.value = controller.repeatMode
        syncCurrentSong(controller)

        controller.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) { _isPlaying.value = isPlaying }
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) { syncCurrentSong(controller) }
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) _duration.value = controller.duration.coerceAtLeast(0L)
            }
            override fun onShuffleModeEnabledChanged(enabled: Boolean) { _shuffleMode.value = enabled }
            override fun onRepeatModeChanged(mode: Int) { _repeatMode.value = mode }
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
            if (controller.duration > 0) _duration.value = controller.duration
        }
    }

    private fun updateProgress() {
        viewModelScope.launch {
            while (true) {
                val controller = mediaController
                if (controller != null && _isPlaying.value) {
                    _currentPosition.value = controller.currentPosition
                }
                delay(1000)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
        mediaController?.release()
    }

    fun setDarkTheme(isDark: Boolean) {
        viewModelScope.launch {
            dataStoreManager.saveTheme(isDark)
        }
    }

    fun setLanguage(code: String) {
        // code là "vi" hoặc "en"
        val localeList = LocaleListCompat.forLanguageTags(code)
        AppCompatDelegate.setApplicationLocales(localeList)
    }
}