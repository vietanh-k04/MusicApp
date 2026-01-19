package com.example.musicapp.ui.viewmodel

import android.content.ComponentName
import android.content.Context
import android.os.CountDownTimer
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.net.toUri
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.musicapp.data.local.source.DataStoreManager
import com.example.musicapp.domain.model.Song
import com.example.musicapp.domain.repository.MusicRepository
import com.example.musicapp.service.MusicService
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: MusicRepository
) : ViewModel() {

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

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite = _isFavorite.asStateFlow()


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

        viewModelScope.launch {
            currentPlayingSong.collectLatest { song ->
                if (song != null) {
                    repository.isFavorite(song.id ?: -1).collect { isLiked ->
                        _isFavorite.value = isLiked
                    }
                } else {
                    _isFavorite.value = false
                }
            }
        }
    }

    fun playMusic(song: Song, playlist: List<Song>) {
        viewModelScope.launch(Dispatchers.IO) {

            val isOnline = song.contentUri?.startsWith("http") == true
            val finalSong: Song?
            val finalPlaylist: List<Song>

            if (isOnline) {
                finalSong = song
                finalPlaylist = playlist
            } else {
                val allLocalSongs = repository.getLocalSongs()
                finalSong = allLocalSongs.find { it.id == song.id }
                    ?: allLocalSongs.find { it.title == song.title && it.artist == song.artist }
                if (finalSong == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Không tìm thấy file nhạc trong máy!", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }
                finalPlaylist = playlist.map { item ->
                    if (item.contentUri?.startsWith("http") == true) {
                        item
                    } else {
                        allLocalSongs.find { it.id == item.id }
                            ?: allLocalSongs.find { it.title == item.title && it.artist == item.artist }
                            ?: item
                    }
                }
            }

            withContext(Dispatchers.Main) {
                val controller = mediaController ?: return@withContext
                _currentPlayingSong.value = finalSong
                _currentPosition.value = 0L
                _duration.value = 0L
                _isPlaying.value = true

                if (controller.currentMediaItem?.mediaId == finalSong.id.toString()) {
                    if (!controller.isPlaying) controller.play()
                    return@withContext
                }

                val index = finalPlaylist.indexOfFirst { it.id == finalSong.id }

                val mediaItems = finalPlaylist.map { item ->
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
                if (index != -1) {
                    controller.seekTo(index, 0L)
                }
                controller.prepare()
                controller.play()
            }
        }
    }

    fun toggleMusic() {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            controller.pause()
        } else {
            controller.play()
        }
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
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                syncCurrentSong(controller)
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    _duration.value = controller.duration.coerceAtLeast(0L)
                }
                if (state == Player.STATE_ENDED) {
                    _isPlaying.value = false
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                _isPlaying.value = false
            }

            override fun onShuffleModeEnabledChanged(enabled: Boolean) { _shuffleMode.value = enabled }
            override fun onRepeatModeChanged(mode: Int) { _repeatMode.value = mode }
        })
    }

    private fun syncCurrentSong(controller: MediaController) {
        val item = controller.currentMediaItem
        if (item != null) {
            val uri = item.localConfiguration?.uri
                ?: item.requestMetadata.mediaUri
                ?: "".toUri()

            val song = Song(
                id = item.mediaId.toLongOrNull(),
                title = item.mediaMetadata.title.toString(),
                artist = item.mediaMetadata.artist.toString(),
                contentUri = uri.toString(),
                albumArtUri = item.mediaMetadata.artworkUri.toString()
            )

            _currentPlayingSong.value = song
            if (controller.duration > 0) _duration.value = controller.duration
            viewModelScope.launch {
                repository.addToHistory(song)
            }
        }
    }

    private fun updateProgress() {
        viewModelScope.launch {
            while (true) {
                val controller = mediaController
                if (controller != null && controller.isPlaying) {
                    _currentPosition.value = controller.currentPosition
                }
                delay(500)
            }
        }
    }

    fun setSleepTimer(minutes: Int) {
        timer?.cancel()
        if (minutes == 0) {
            _remainingTime.value = null
            return
        }
        val millis = minutes * 60 * 1000L
        timer = object : CountDownTimer(millis, 1000) {
            override fun onTick(millisUntilFinished: Long) { _remainingTime.value = millisUntilFinished }
            override fun onFinish() {
                _remainingTime.value = null
                mediaController?.pause()
            }
        }.start()
    }

    fun cancelSleepTimer() {
        timer?.cancel()
        _remainingTime.value = null
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
        mediaController?.release()
    }

    fun toggleFavorite() {
        val song = currentPlayingSong.value ?: return
        viewModelScope.launch {
            if (_isFavorite.value) repository.removeFavorite(song.id ?: -1)
            else repository.insertFavorite(song)
        }
    }

    fun stopMusic() {
        mediaController?.pause()
        _isPlaying.value = false
        _currentPlayingSong.value = null
        _currentPosition.value = 0L
    }
}