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
fun FavoriteScreen(
    viewModel: LibraryViewModel = hiltViewModel(),
    sharedViewModel: SharedViewModel,
    onBack: () -> Unit
) {
    val favorites by viewModel.favoriteSongs.collectAsState()

    SimpleListScreen(title = "Bài hát yêu thích", onBack = onBack) {
        if (favorites.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Chưa có bài hát yêu thích nào", color = Color.Gray)
            }
        } else {
            LazyColumn {
                items(favorites) { entity ->
                    val song = Song(entity.id, entity.title, entity.artist, entity.contentUri, entity.albumArtUri)
                    SongItem(song = song, onClick = {
                        // Phát nhạc: Chuyển toàn bộ list favorite thành playlist
                        val playlist = favorites.map { Song(it.id, it.title, it.artist, it.contentUri, it.albumArtUri) }
                        sharedViewModel.playMusic(song, playlist)
                    })
                }
            }
        }
    }
}