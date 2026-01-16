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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicapp.ui.components.SongItem
import com.example.musicapp.ui.viewmodel.LibraryUiState
import com.example.musicapp.ui.viewmodel.LibraryViewModel
import com.example.musicapp.ui.viewmodel.SharedViewModel
import com.example.musicapp.R
import com.example.musicapp.ui.navigation.Screen

@Composable
fun LibraryScreen(libraryViewModel: LibraryViewModel = hiltViewModel(), sharedViewModel: SharedViewModel, onNavigateTo: (String) -> Unit) {
    val uiState by libraryViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
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
                text = stringResource(R.string.lib_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LibraryShortcut(icon = Icons.Default.Favorite, title = stringResource(R.string.lib_favorites), color = Color(0xFFFF5252)) {
                onNavigateTo(Screen.Favorite.route)
            }
            LibraryShortcut(icon = Icons.AutoMirrored.Filled.QueueMusic, stringResource(R.string.lib_playlist), color = Color(0xFF448AFF)) {
                onNavigateTo(Screen.Playlists.route)
            }
            LibraryShortcut(icon = Icons.Default.Album, title = stringResource(R.string.lib_album), color = Color(0xFFFFAB40)) {
                onNavigateTo(Screen.Albums.route)
            }
            LibraryShortcut(icon = Icons.Default.History, title = stringResource(R.string.lib_recent), color = Color(0xFF69F0AE)) {
                onNavigateTo(Screen.History.route)
            }
        }

        HorizontalDivider(thickness = 0.5.dp, color = Color.Gray.copy(alpha = 0.3f))

        // 3. DANH SÁCH BÀI HÁT
        Text(
            text = stringResource(R.string.lib_local_songs),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(16.dp)
        )

        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            when (uiState) {
                is LibraryUiState.Success -> {
                    val songs = (uiState as LibraryUiState.Success).songs
                    if (songs.isEmpty()) {
                        Text(
                            stringResource(R.string.error_song),
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn {
                            items(songs) { song ->
                                SongItem(
                                    song = song,
                                    onClick = {
                                        sharedViewModel.playMusic(song, songs)
                                    }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(100.dp)) }
                        }
                    }
                }
                is LibraryUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is LibraryUiState.Error -> {
                    Text(
                        stringResource(R.string.error_music),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun LibraryShortcut(icon: ImageVector, title: String, color: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(color.copy(alpha = 0.1f)),
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