package com.example.musicapp.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.musicapp.R

sealed class Screen(val route: String, @StringRes val title: Int, val icon: ImageVector) {
    object Library : Screen("library", R.string.nav_library, Icons.Default.Person)
    object Discovery : Screen("discovery", R.string.nav_discovery, Icons.Default.Search)
    object Charts : Screen("charts", R.string.nav_charts, Icons.Default.BarChart)
    object More : Screen("more", R.string.nav_more, Icons.Default.Settings)
}