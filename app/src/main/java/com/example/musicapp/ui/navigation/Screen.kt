package com.example.musicapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Library : Screen("library", "Cá nhân", Icons.Default.Person)
    object Discovery : Screen("discovery", "Khám phá", Icons.Default.Search)
    object Charts : Screen("charts", "#BXH", Icons.Default.BarChart)
    object More : Screen("more", "Thêm", Icons.Default.Settings)
}