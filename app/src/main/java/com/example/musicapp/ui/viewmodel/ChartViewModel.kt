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

data class ChartCategory(val name: String, val query: String)

@HiltViewModel
class ChartViewModel @Inject constructor(private val repository: MusicRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<ChartUiState>(ChartUiState.Loading)
    val uiState = _uiState.asStateFlow()

    val categories = listOf(
        ChartCategory("V-Pop", "Top Hits Vietnam"),
        ChartCategory("US-UK", "Top Hits US UK"),
        ChartCategory("K-Pop", "K-Pop Top 100"),
        ChartCategory("Rap Việt", "Rap Viet Hot"),
        ChartCategory("Indie", "Indie Vietnam")
    )

    private val _selectedCategory = MutableStateFlow(categories[0])
    val selectedCategory = _selectedCategory.asStateFlow()

    init {
        loadChartData(categories[0])
    }

    fun loadChartData(category: ChartCategory) {
        _selectedCategory.value = category

        viewModelScope.launch {
            _uiState.value = ChartUiState.Loading
            try {
                val songs = repository.searchSongs(category.query)
                if (songs.isNotEmpty()) {
                    _uiState.value = ChartUiState.Success(songs)
                } else {
                    _uiState.value = ChartUiState.Error
                }
            } catch (_: Exception) {
                _uiState.value = ChartUiState.Error
            }
        }
    }
}