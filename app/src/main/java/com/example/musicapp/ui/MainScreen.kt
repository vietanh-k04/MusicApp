package com.example.musicapp.ui

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.musicapp.ui.components.MiniPlayer
import com.example.musicapp.ui.screens.LibraryScreen
import com.example.musicapp.ui.navigation.Screen
import com.example.musicapp.ui.screens.ChartScreen
import com.example.musicapp.ui.screens.DiscoveryScreen
import com.example.musicapp.ui.screens.MoreScreen
import com.example.musicapp.ui.viewmodel.SharedViewModel
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.musicapp.ui.screens.AppHeader
import com.example.musicapp.ui.screens.libraryChildScreens.AlbumDetailScreen
import com.example.musicapp.ui.screens.libraryChildScreens.AlbumScreen
import com.example.musicapp.ui.screens.libraryChildScreens.FavoriteScreen
import com.example.musicapp.ui.screens.libraryChildScreens.HistoryScreen
import com.example.musicapp.ui.screens.libraryChildScreens.PlaylistDetailScreen
import com.example.musicapp.ui.screens.libraryChildScreens.PlaylistScreen
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import com.example.musicapp.ui.viewmodel.SettingsViewModel

@Composable
fun MainScreen(
    sharedViewModel: SharedViewModel,
    settingsViewModel: SettingsViewModel,
    onFullScreenPlayerRequest: () -> Unit
) {
    val navController = rememberNavController()
    val items = listOf(Screen.Library, Screen.Discovery, Screen.Charts, Screen.More)

    val currentSong by sharedViewModel.currentPlayingSong.collectAsState()
    val isPlaying by sharedViewModel.isPlaying.collectAsState()
    val currentPos by sharedViewModel.currentPosition.collectAsState()
    val duration by sharedViewModel.duration.collectAsState()

    Scaffold(
        topBar = {
            AppHeader()
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { stringResource(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            NavHost(navController = navController, startDestination = Screen.Library.route) {

                // Tab 1: Thư viện
                composable(Screen.Library.route) {
                    LibraryScreen(sharedViewModel = sharedViewModel, onNavigateTo = { route -> navController.navigate(route) })
                }

                composable(Screen.Favorite.route) {
                    FavoriteScreen(sharedViewModel = sharedViewModel, onBack = { navController.popBackStack() })
                }
                composable(Screen.History.route) {
                    HistoryScreen(sharedViewModel = sharedViewModel, onBack = { navController.popBackStack() })
                }
                composable(Screen.Albums.route) {
                    AlbumScreen(
                        onBack = { navController.popBackStack() },
                        onAlbumClick = { _, artistName ->
                            val encodedName = Uri.encode(artistName)
                            navController.navigate(Screen.AlbumDetail.createRoute(encodedName))
                        }
                    )
                }

                composable(Screen.Playlists.route) {
                    PlaylistScreen(
                        onBack = { navController.popBackStack() },
                        onNavigateToDetail = { id, name ->
                            navController.navigate(Screen.PlaylistDetail.createRoute(id, name))
                        }
                    )
                }

                composable(Screen.Discovery.route) {
                    DiscoveryScreen(sharedViewModel = sharedViewModel)
                }

                composable(Screen.Charts.route) {
                    ChartScreen(sharedViewModel = sharedViewModel)
                }

                composable(Screen.More.route) {
                    MoreScreen(sharedViewModel = sharedViewModel, settingsViewModel = settingsViewModel)
                }

                composable(
                    route = Screen.PlaylistDetail.route,
                    arguments = listOf(
                        navArgument("id") { type = NavType.LongType },
                        navArgument("name") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getLong("id") ?: 0L
                    val name = backStackEntry.arguments?.getString("name") ?: "Playlist"

                    PlaylistDetailScreen(
                        playlistId = id,
                        playlistName = name,
                        onBack = { navController.popBackStack() },
                        sharedViewModel = sharedViewModel
                    )
                }

                composable(
                    route = Screen.AlbumDetail.route,
                    arguments = listOf(
                        navArgument("name") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val artistName = backStackEntry.arguments?.getString("name") ?: ""

                    AlbumDetailScreen(
                        artistName = artistName,
                        onBack = { navController.popBackStack() },
                        sharedViewModel = sharedViewModel
                    )
                }
            }
            if (currentSong != null) {
                Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                    val progress = if (duration > 0) currentPos.toFloat() / duration.toFloat() else 0f

                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.EndToStart) {
                                sharedViewModel.stopMusic()
                                true
                            } else {
                                false
                            }
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        enableDismissFromEndToStart = true,
                        backgroundContent = {
                            Box(modifier = Modifier.fillMaxSize().padding(16.dp))
                        },
                        content = {
                            MiniPlayer(
                                song = currentSong!!,
                                isPlaying = isPlaying,
                                progress = progress,
                                onTogglePlay = { sharedViewModel.toggleMusic() },
                                onNext = { sharedViewModel.skipToNext() },
                                onPrev = { sharedViewModel.skipToPrevious() },
                                onClick = { onFullScreenPlayerRequest() }
                            )
                        }
                    )
                }
            }
        }
    }
}
