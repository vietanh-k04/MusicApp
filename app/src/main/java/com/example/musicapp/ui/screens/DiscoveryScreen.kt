package com.example.musicapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicapp.ui.components.SongItem
import com.example.musicapp.ui.viewmodel.DiscoveryViewModel
import com.example.musicapp.ui.viewmodel.SearchUiState
import com.example.musicapp.ui.viewmodel.SharedViewModel
import com.example.musicapp.R

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryScreen(discoveryViewModel: DiscoveryViewModel = hiltViewModel(), sharedViewModel: SharedViewModel) {
    val searchState by discoveryViewModel.uiState.collectAsState()
    val suggestedState by discoveryViewModel.suggestedUiState.collectAsState()

    var query by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 1. THANH TÌM KIẾM
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(stringResource(R.string.find_singer_song)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = {
                            query = ""
                            discoveryViewModel.searchOnline("")
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    discoveryViewModel.searchOnline(query)
                    focusManager.clearFocus()
                })
            )
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {

            if (query.isNotEmpty()) {
                when (searchState) {
                    is SearchUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    is SearchUiState.Error -> Text(stringResource(R.string.error_find), modifier = Modifier.align(Alignment.Center))
                    is SearchUiState.Success -> {
                        val songs = (searchState as SearchUiState.Success).songs
                        LazyColumn(contentPadding = PaddingValues(bottom = 100.dp)) {
                            items(songs) { song ->
                                SongItem(song = song, onClick = { sharedViewModel.playMusic(song, songs) })
                            }
                        }
                    }
                    else -> {}
                }
            }
            else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    item {
                        SuggestionView(onKeywordClick = { keyword ->
                            query = keyword
                            discoveryViewModel.searchOnline(keyword)
                        })
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                stringResource(R.string.want_listen),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            // Nút làm mới danh sách gợi ý
                            IconButton(onClick = { discoveryViewModel.loadRandomSuggestion() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                            }
                        }
                    }

                    // Phần C: Hiển thị list nhạc gợi ý
                    when (suggestedState) {
                        is SearchUiState.Success -> {
                            val songs = (suggestedState as SearchUiState.Success).songs
                            items(songs) { song ->
                                SongItem(song = song, onClick = { sharedViewModel.playMusic(song, songs) })
                            }
                        }
                        is SearchUiState.Loading -> {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                        is SearchUiState.Error -> {
                            item {
                                Text(stringResource(R.string.error_sugges), modifier = Modifier.padding(16.dp), color = Color.Gray)
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SuggestionView(onKeywordClick: (String) -> Unit) {
    val keywords = listOf("Sơn Tùng M-TP", "Mono", "Hieuthuhai", "V-Pop", "US-UK", "Imagine Dragons")

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            stringResource(R.string.search_sugges),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            keywords.forEach { keyword ->
                SuggestionChip(
                    onClick = { onKeywordClick(keyword) },
                    label = { Text(keyword) }
                )
            }
        }
    }
}