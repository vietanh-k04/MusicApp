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

sealed interface ChartUiState {
    object Loading : ChartUiState
    data class Success(val songs: List<Song>) : ChartUiState
    object Error : ChartUiState
}

@HiltViewModel
class ChartViewModel @Inject constructor(private val repository: MusicRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<ChartUiState>(ChartUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadChartData()
    }

    fun loadChartData() {
        viewModelScope.launch {
            _uiState.value = ChartUiState.Loading
            try {
                // Giả lập BXH bằng cách tìm các bài hát Hot nhất hiện nay
                val songs = repository.searchSongs("Top Hits Vietnam 2024")
                if (songs.isNotEmpty()) {
                    _uiState.value = ChartUiState.Success(songs)
                } else {
                    _uiState.value = ChartUiState.Error
                }
            } catch (e: Exception) {
                _uiState.value = ChartUiState.Error
            }
        }
    }
}