package com.example.musicapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicapp.ui.components.SongItem
import com.example.musicapp.ui.viewmodel.LibraryUiState
import com.example.musicapp.ui.viewmodel.LibraryViewModel
import com.example.musicapp.ui.viewmodel.SharedViewModel

@Composable
fun LibraryScreen(libraryViewModel: LibraryViewModel = hiltViewModel(), sharedViewModel: SharedViewModel) {
    val uiState by libraryViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Nền theo giao diện máy (Trắng/Đen)
    ) {
        // 1. HEADER (Tiêu đề có Gradient nhẹ)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(top = 16.dp, bottom = 8.dp)
        ) {
            Text(
                text = "Thư viện",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // 2. MENU NGANG (Các phím tắt)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LibraryShortcut(icon = Icons.Default.Favorite, title = "Yêu thích", color = Color(0xFFFF5252)) {}
            LibraryShortcut(icon = Icons.AutoMirrored.Filled.QueueMusic, title = "Playlist", color = Color(0xFF448AFF)) {}
            LibraryShortcut(icon = Icons.Default.Album, title = "Album", color = Color(0xFFFFAB40)) {}
            LibraryShortcut(icon = Icons.Default.History, title = "Gần đây", color = Color(0xFF69F0AE)) {}
        }

        HorizontalDivider(thickness = 0.5.dp, color = Color.Gray.copy(alpha = 0.3f))

        // 3. DANH SÁCH BÀI HÁT
        Text(
            text = "Nhạc trong máy",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(16.dp)
        )

        // Vùng hiển thị danh sách
        Box(modifier = Modifier.weight(1f)) {
            when (uiState) {
                is LibraryUiState.Success -> {
                    val songs = (uiState as LibraryUiState.Success).songs
                    if (songs.isEmpty()) {
                        Text(
                            "Không tìm thấy bài hát nào trong máy",
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn {
                            items(songs) { song ->
                                SongItem(
                                    song = song,
                                    onClick = {
                                        // Khi bấm vào bài hát:
                                        // Gọi SharedViewModel để phát bài đó
                                        // Và truyền luôn cả danh sách (songs) để nó biết bài tiếp theo là gì
                                        sharedViewModel.playMusic(song, songs)
                                    }
                                )
                            }
                            // Thêm khoảng trống dưới cùng để MiniPlayer không che mất bài cuối
                            item { Spacer(modifier = Modifier.height(100.dp)) }
                        }
                    }
                }
                is LibraryUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is LibraryUiState.Error -> {
                    Text(
                        "Lỗi khi tải nhạc hoặc chưa cấp quyền!",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

// Component con để vẽ các nút menu (Yêu thích, Album...)
@Composable
fun LibraryShortcut(icon: ImageVector, title: String, color: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(color.copy(alpha = 0.1f)), // Nền mờ theo màu icon
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}