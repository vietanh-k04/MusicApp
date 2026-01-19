package com.example.musicapp.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.musicapp.R

sealed class Screen(val route: String, @StringRes val title: Int, val icon: ImageVector) {
    object Library : Screen("library", R.string.nav_library, Icons.Default.Person)
    object Discovery : Screen("discovery", R.string.nav_discovery, Icons.Default.Search)
    object Charts : Screen("charts", R.string.nav_charts, Icons.Default.BarChart)
    object More : Screen("more", R.string.nav_more, Icons.Default.Settings)

    object Favorite : Screen("favorite", R.string.lib_favorites, Icons.Default.Favorite)
    object History : Screen("history", R.string.lib_recent, Icons.Default.History)
    object Albums : Screen("albums", R.string.lib_album, Icons.Default.Album)
    object Playlists : Screen("playlists", R.string.lib_playlist, Icons.AutoMirrored.Filled.List)

    object PlaylistDetail : Screen("playlist_detail/{id}/{name}", R.string.lib_playlist, Icons.AutoMirrored.Filled.List) {
        fun createRoute(id: Long, name: String) = "playlist_detail/$id/$name"
    }

    object AlbumDetail : Screen("album_detail/{name}", R.string.lib_album, Icons.Default.Album) {
        fun createRoute(name: String) = "album_detail/$name"
    }
}