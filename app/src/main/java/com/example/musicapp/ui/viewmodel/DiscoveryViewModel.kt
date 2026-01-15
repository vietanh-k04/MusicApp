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

sealed interface SearchUiState {
    object Idle : SearchUiState
    object Loading : SearchUiState
    data class Success(val songs: List<Song>) : SearchUiState
    object Error : SearchUiState
}

@HiltViewModel
class DiscoveryViewModel @Inject constructor(private val repository: MusicRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _suggestedUiState = MutableStateFlow<SearchUiState>(SearchUiState.Loading)
    val suggestedUiState = _suggestedUiState.asStateFlow()

    init {
        loadRandomSuggestion()
    }

    fun searchOnline(query: String) {
        if (query.isBlank()) {
            _uiState.value = SearchUiState.Idle
            return
        }
        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            try {
                val songs = repository.searchSongs(query)
                if (songs.isEmpty()) {
                    _uiState.value = SearchUiState.Error
                } else {
                    _uiState.value = SearchUiState.Success(songs)
                }
            } catch (e: Exception) {
                _uiState.value = SearchUiState.Error
            }
        }
    }

    fun loadRandomSuggestion() {
        // Danh sách từ khóa để "random"
        val randomKeywords = listOf("V-Pop", "Indie Vietnam", "US-UK Top Hits", "K-Pop", "Chill", "Rap Việt")
        val randomKey = randomKeywords.random()

        viewModelScope.launch {
            _suggestedUiState.value = SearchUiState.Loading
            try {
                val songs = repository.searchSongs(randomKey)
                if (songs.isNotEmpty()) {
                    _suggestedUiState.value = SearchUiState.Success(songs)
                } else {
                    _suggestedUiState.value = SearchUiState.Error
                }
            } catch (e: Exception) {
                _suggestedUiState.value = SearchUiState.Error
            }
        }
    }
}