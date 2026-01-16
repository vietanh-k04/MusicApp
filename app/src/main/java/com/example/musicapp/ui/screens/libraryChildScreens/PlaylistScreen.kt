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
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicapp.ui.viewmodel.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(
    viewModel: LibraryViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val playlists by viewModel.playlists.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Playlist của tôi") },
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
                Text("Chưa có Playlist nào. Hãy tạo mới!", color = Color.Gray)
            }
        } else {
            LazyColumn(contentPadding = padding) {
                items(playlists) { playlist ->
                    ListItem(
                        leadingContent = {
                            Icon(Icons.Default.MusicNote, null, modifier = Modifier.size(40.dp).background(Color.LightGray, RoundedCornerShape(8.dp)).padding(8.dp))
                        },
                        headlineContent = { Text(playlist.playlistName, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("Được tạo lúc: ...") },
                        modifier = Modifier.clickable { /* TODO: Mở chi tiết playlist */ }
                    )
                    HorizontalDivider()
                }
            }
        }

        // Dialog tạo Playlist
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Tạo Playlist mới") },
                text = {
                    OutlinedTextField(
                        value = newPlaylistName,
                        onValueChange = { newPlaylistName = it },
                        label = { Text("Tên Playlist") },
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
                    }) { Text("Tạo") }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) { Text("Hủy") }
                }
            )
        }
    }
}