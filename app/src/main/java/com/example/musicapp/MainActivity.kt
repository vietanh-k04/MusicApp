package com.example.musicapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.musicapp.ui.home.HomeScreen
import com.example.musicapp.ui.home.HomeViewModel
import com.example.musicapp.ui.player.PlayerScreen
import com.example.musicapp.ui.theme.MusicAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MusicAppTheme {
                val navController = rememberNavController()

                val sharedViewModel: HomeViewModel = hiltViewModel()

                NavHost(navController = navController, startDestination = "home") {

                    composable("home") {
                        HomeScreen(
                            // 2. TRUYỀN VIEWMODEL VÀO
                            viewModel = sharedViewModel,
                            onMiniPlayerClick = {
                                navController.navigate("player")
                            }
                        )
                    }

                    composable(
                        route = "player",
                        enterTransition = {
                            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(500))
                        },
                        exitTransition = {
                            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(500))
                        }
                    ) {
                        PlayerScreen(
                            onBack = { navController.popBackStack() },
                            // 3. TRUYỀN CÙNG 1 INSTANCE VIEWMODEL VÀO ĐÂY
                            viewModel = sharedViewModel
                        )
                    }
                }
            }
        }
    }
}