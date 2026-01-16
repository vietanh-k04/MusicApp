package com.example.musicapp.ui.screens.libraryChildScreens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicapp.domain.model.Song
import com.example.musicapp.ui.components.SongItem
import com.example.musicapp.ui.viewmodel.LibraryViewModel
import com.example.musicapp.ui.viewmodel.SharedViewModel

@Composable
fun HistoryScreen(
    viewModel: LibraryViewModel = hiltViewModel(),
    sharedViewModel: SharedViewModel,
    onBack: () -> Unit
) {
    val history by viewModel.historySongs.collectAsState()

    SimpleListScreen(title = "Nghe gần đây", onBack = onBack) {
        if (history.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Chưa nghe bài hát nào", color = Color.Gray)
            }
        } else {
            LazyColumn {
                items(history) { entity ->
                    val song = Song(entity.id, entity.title, entity.artist, entity.contentUri, entity.albumArtUri)
                    SongItem(song = song, onClick = {
                        val playlist = history.map { Song(it.id, it.title, it.artist, it.contentUri, it.albumArtUri) }
                        sharedViewModel.playMusic(song, playlist)
                    })
                }
            }
        }
    }
}