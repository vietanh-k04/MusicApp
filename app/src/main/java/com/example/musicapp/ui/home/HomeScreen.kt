package com.example.musicapp.ui.home

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicapp.ui.components.SongItem

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

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

    // 4. Vẽ UI dựa trên State
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (uiState) {
            is HomeUiState.Loading -> {
                CircularProgressIndicator()
            }
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
        }
    }
}