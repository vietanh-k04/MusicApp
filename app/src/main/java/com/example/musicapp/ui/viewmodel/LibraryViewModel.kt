package com.example.musicapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.domain.model.Song
import com.example.musicapp.domain.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    init {
        loadLocalSongs()
    }

    fun loadLocalSongs() {
        viewModelScope.launch {
            _uiState.value = LibraryUiState.Loading
            try {
                val songs = repository.getLocalSongs()
                _uiState.value = LibraryUiState.Success(songs)
            } catch (e: Exception) {
                _uiState.value = LibraryUiState.Error
            }
        }
    }
}