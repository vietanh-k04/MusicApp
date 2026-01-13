package com.example.musicapp.ui.home

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.musicapp.ui.components.MiniPlayer
import com.example.musicapp.ui.components.SongItem

@Composable
fun HomeScreen(viewModel: HomeViewModel, onMiniPlayerClick: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()

    val currentSong by viewModel.currentPlayingSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    val currentPos by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()

    var searchQuery by remember { mutableStateOf("Son Tung MTP") }

    // 1. Xác định quyền cần xin dựa trên phiên bản Android
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    // 2. Tạo launcher để xin quyền
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            viewModel.loadSongs()
        } else {
            viewModel.onPermissionDenied()
        }
    }

    // 3. Tự động xin quyền ngay khi màn hình mở lên
    LaunchedEffect(Unit) {
        permissionLauncher.launch(permission)
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // 1. THANH TÌM KIẾM (Đặt ở trên cùng)
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Nhập tên bài hát...") },
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = { viewModel.searchOnline(searchQuery) }, // Gọi hàm tìm kiếm
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
        }

        // 2. PHẦN HIỂN THỊ LIST NHẠC (Giữ nguyên logic cũ, chỉ bọc trong Box còn lại)
        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            when (uiState) {
                is HomeUiState.Success -> {
                    val songs = (uiState as HomeUiState.Success).songs
                    if (songs.isEmpty()) {
                        Text("Không tìm thấy bài hát nào trong máy!")
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(songs) { song ->
                                SongItem(
                                    song = song,
                                    onClick = {
                                        viewModel.playMusic(song)
                                    })
                            }
                        }
                    }
                }
                is HomeUiState.Error -> {
                    Text("Có lỗi xảy ra khi tải nhạc!")
                }
                is HomeUiState.PermissionRequired -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Ứng dụng cần quyền đọc file để phát nhạc.")
                        Button(onClick = { permissionLauncher.launch(permission) }) {
                            Text("Cấp quyền")
                        }
                    }
                }
                is HomeUiState.Loading -> CircularProgressIndicator()
            }
        }
        if (currentSong != null) {
            val progress = if (duration > 0) currentPos.toFloat() / duration.toFloat() else 0f
            MiniPlayer(
                song = currentSong!!,
                isPlaying = isPlaying,
                progress = progress,
                onTogglePlay = { viewModel.toggleMusic() },
                onNext = { viewModel.skipToNext() },
                onPrev = { viewModel.skipToPrevious() },
                onClick = { onMiniPlayerClick() }
            )
        }
    }
}