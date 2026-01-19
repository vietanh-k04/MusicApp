package com.example.musicapp.ui.screens.libraryChildScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicapp.R
import com.example.musicapp.ui.viewmodel.LibraryViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(
    viewModel: LibraryViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNavigateToDetail: (Long, String) -> Unit
) {
    val playlists by viewModel.playlists.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.playlist_hint)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Create")
            }
        }
    ) { padding ->
        if (playlists.isEmpty()) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.playlist_announcement), color = Color.Gray)
            }
        } else {
            LazyColumn(contentPadding = padding) {
                items(playlists) { playlist ->
                    ListItem(
                        leadingContent = {
                            Icon(Icons.Default.MusicNote, null, modifier = Modifier.size(40.dp).background(Color.LightGray, RoundedCornerShape(8.dp)).padding(8.dp))
                        },
                        headlineContent = { Text(playlist.playlistName, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("ID: ${playlist.playlistId}") },
                        modifier = Modifier.clickable {
                            onNavigateToDetail(playlist.playlistId, playlist.playlistName)
                        },
                        trailingContent = {
                            IconButton(onClick = {
                                viewModel.deletePlaylist(playlist.playlistId)
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(stringResource(R.string.playlist_create)) },
                text = {
                    OutlinedTextField(
                        value = newPlaylistName,
                        onValueChange = { newPlaylistName = it },
                        label = { Text(stringResource(R.string.playlist_name)) },
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            viewModel.createPlaylist(newPlaylistName)
                            newPlaylistName = ""
                            showDialog = false
                        }
                    }) { Text(stringResource(R.string.playlist_submit)) }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) { stringResource(R.string.playlist_dismiss) }
                }
            )
        }
    }
}