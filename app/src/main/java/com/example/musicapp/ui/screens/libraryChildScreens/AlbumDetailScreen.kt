package com.example.musicapp.ui.screens.libraryChildScreens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicapp.R
import com.example.musicapp.ui.components.SongItem
import com.example.musicapp.ui.viewmodel.LibraryViewModel
import com.example.musicapp.ui.viewmodel.SharedViewModel

@Composable
fun AlbumDetailScreen(
    artistName: String,
    onBack: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
    sharedViewModel: SharedViewModel
) {
    LaunchedEffect(artistName) {
        viewModel.loadSongsByArtist(artistName)
    }

    val songs by viewModel.artistSongs.collectAsState()

    SimpleListScreen(title = "${stringResource(R.string.albumn_hint)} $artistName", onBack = onBack) {
        if (songs.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.albumn_load), color = Color.Gray)
            }
        } else {
            LazyColumn {
                items(songs) { song ->
                    SongItem(song = song, onClick = {
                        sharedViewModel.playMusic(song, songs)
                    })
                }
            }
        }
    }
}