package com.example.musicapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicapp.ui.components.ChartSongItem
import com.example.musicapp.ui.viewmodel.ChartUiState
import com.example.musicapp.ui.viewmodel.ChartViewModel
import com.example.musicapp.ui.viewmodel.SharedViewModel
import com.example.musicapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartScreen(
    chartViewModel: ChartViewModel = hiltViewModel(),
    sharedViewModel: SharedViewModel
) {
    val uiState by chartViewModel.uiState.collectAsState()
    val selectedCategory by chartViewModel.selectedCategory.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF8E2DE2),
                            Color(0xFF4A00E0)
                        )
                    )
                ),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = stringResource(R.string.tops_hint),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chartViewModel.categories) { category ->
                val isSelected = category == selectedCategory

                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (!isSelected) {
                            chartViewModel.loadChartData(category)
                        }
                    },
                    label = { Text(category.name) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) Color.Transparent else Color.Gray.copy(alpha = 0.5f)
                    )
                )
            }
        }

        HorizontalDivider(thickness = 0.5.dp, color = Color.Gray.copy(alpha = 0.2f))

        Box(modifier = Modifier.weight(1f)) {
            when (uiState) {
                is ChartUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ChartUiState.Success -> {
                    val songs = (uiState as ChartUiState.Success).songs

                    LazyColumn(contentPadding = PaddingValues(bottom = 100.dp)) {
                        itemsIndexed(songs) { index, song ->
                            ChartSongItem(
                                song = song,
                                rank = index + 1,
                                onClick = {
                                    sharedViewModel.playMusic(song, songs)
                                }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 60.dp),
                                thickness = 0.5.dp,
                                color = Color.Gray.copy(alpha = 0.1f)
                            )
                        }
                    }
                }
                is ChartUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(stringResource(R.string.error_data), color = Color.Gray)
                        Button(
                            onClick = { chartViewModel.loadChartData(selectedCategory) },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text(stringResource(R.string.error_again))
                        }
                    }
                }
            }
        }
    }
}