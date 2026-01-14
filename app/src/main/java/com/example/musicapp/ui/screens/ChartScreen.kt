package com.example.musicapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicapp.ui.components.ChartSongItem
import com.example.musicapp.ui.viewmodel.ChartUiState
import com.example.musicapp.ui.viewmodel.ChartViewModel
import com.example.musicapp.ui.viewmodel.SharedViewModel

@Composable
fun ChartScreen(
    chartViewModel: ChartViewModel = hiltViewModel(),
    sharedViewModel: SharedViewModel // Để phát nhạc
) {
    val uiState by chartViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 1. HEADER #BXH
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp) // Header cao một chút
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF8E2DE2), // Màu tím đậm
                            Color(0xFF4A00E0)  // Màu xanh tím
                        )
                    )
                ),
            contentAlignment = Alignment.BottomStart
        ) {
            Text(
                text = "#BXH Top Hits",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(20.dp)
            )
        }

        // 2. DANH SÁCH BÀI HÁT
        Box(modifier = Modifier.weight(1f)) {
            when (uiState) {
                is ChartUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ChartUiState.Success -> {
                    val songs = (uiState as ChartUiState.Success).songs

                    LazyColumn(contentPadding = PaddingValues(bottom = 100.dp)) {
                        // itemsIndexed cung cấp cả chỉ số (index) để ta tính rank
                        itemsIndexed(songs) { index, song ->
                            ChartSongItem(
                                song = song,
                                rank = index + 1, // Rank bắt đầu từ 1
                                onClick = {
                                    // Phát nhạc với playlist là danh sách Chart
                                    sharedViewModel.playMusic(song, songs)
                                }
                            )
                            // Thêm đường kẻ mờ giữa các bài
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 60.dp),
                                thickness = 0.5.dp,
                                color = Color.Gray.copy(alpha = 0.1f)
                            )
                        }
                    }
                }
                is ChartUiState.Error -> {
                    Text(
                        "Không tải được bảng xếp hạng",
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    Button(
                        onClick = { chartViewModel.loadChartData() },
                        modifier = Modifier.align(Alignment.Center).padding(top = 80.dp)
                    ) {
                        Text("Thử lại")
                    }
                }
            }
        }
    }
}