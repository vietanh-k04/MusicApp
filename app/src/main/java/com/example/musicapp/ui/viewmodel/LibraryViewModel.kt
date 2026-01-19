package com.example.musicapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.domain.model.Album
import com.example.musicapp.domain.model.Song
import com.example.musicapp.domain.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface LibraryUiState {
    object Loading : LibraryUiState
    data class Success(val songs: List<Song>) : LibraryUiState
    object Error : LibraryUiState
}

@HiltViewModel
class LibraryViewModel @Inject constructor(private val repository: MusicRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<LibraryUiState>(LibraryUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _albumsState = MutableStateFlow<List<Album>>(emptyList())
    val albumsState = _albumsState.asStateFlow()

    val favoriteSongs = repository.getAllFavorites()
        .map { list ->
            list.map { entity ->
                Song(entity.id, entity.title, entity.artist, entity.contentUri, entity.albumArtUri)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val historySongs = repository.getHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists = repository.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _artistSongs = MutableStateFlow<List<Song>>(emptyList())
    val artistSongs = _artistSongs.asStateFlow()

    init {
        loadLocalData()
    }

    fun loadLocalData() {
        viewModelScope.launch {
            _uiState.value = LibraryUiState.Loading
            try {
                val songs = repository.getLocalSongs()
                val albums = repository.getLocalAlbums()

                _uiState.value = LibraryUiState.Success(songs)
                _albumsState.value = albums
            } catch (e: Exception) {
                _uiState.value = LibraryUiState.Error
            }
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            repository.createPlaylist(name)
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun getPlaylistSongs(playlistId: Long): Flow<List<Song>> {
        return repository.getSongsByPlaylistId(playlistId)
    }

    fun addSongToPlaylist(playlistId: Long, song: Song) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, song)
        }
    }

    fun loadSongsByArtist(artistName: String) {
        viewModelScope.launch {
            _artistSongs.value = repository.getSongsByArtist(artistName)
        }
    }
}