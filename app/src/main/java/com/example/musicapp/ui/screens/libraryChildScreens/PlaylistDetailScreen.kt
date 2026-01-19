package com.example.musicapp.ui.screens.libraryChildScreens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
fun PlaylistDetailScreen(
    playlistId: Long,
    playlistName: String,
    onBack: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
    sharedViewModel: SharedViewModel
) {
    val songs by viewModel.getPlaylistSongs(playlistId).collectAsState(initial = emptyList())

    SimpleListScreen(title = playlistName, onBack = onBack) {
        if (songs.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.playlist_empty), color = Color.Gray)
            }
        } else {
            LazyColumn {
                items(songs) { song ->
                    SongItem(song = song, onClick = {
                        sharedViewModel.playMusic(song, songs)
                    },
                        trailingContent = {
                            IconButton(onClick = {
                                viewModel.removeSongFromPlaylist(playlistId, song.id ?: 0)
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove",
                                    tint = Color.Gray
                                )
                            }
                        })
                }
            }
        }
    }
}