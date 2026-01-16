package com.example.musicapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.domain.model.Album
import com.example.musicapp.domain.model.Song
import com.example.musicapp.domain.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
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
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val historySongs = repository.getHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists = repository.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
}