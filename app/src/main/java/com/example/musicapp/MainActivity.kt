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
import com.example.musicapp.ui.MainScreen
import com.example.musicapp.ui.player.PlayerScreen
import com.example.musicapp.ui.theme.MusicAppTheme
import com.example.musicapp.ui.viewmodel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MusicAppTheme {
                val rootNavController = rememberNavController()

                val sharedViewModel: SharedViewModel = hiltViewModel()

                NavHost(
                    navController = rootNavController,
                    startDestination = "main"
                ) {
                    // 1. ROUTE "main"
                    composable("main") {
                        MainScreen(
                            // Truyền ViewModel đã tạo xuống
                            sharedViewModel = sharedViewModel,
                            onFullScreenPlayerRequest = {
                                rootNavController.navigate("player")
                            }
                        )
                    }

                    // 2. ROUTE "player"
                    composable(
                        route = "player",
                        enterTransition = {
                            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(400))
                        },
                        exitTransition = {
                            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(400))
                        },
                        popEnterTransition = {
                            // Sửa lại null hoặc hiệu ứng giữ nguyên để mượt hơn
                            null
                        },
                        popExitTransition = {
                            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(400))
                        }
                    ) {
                        // Không tạo mới viewModel ở đây nữa, dùng cái đã có
                        PlayerScreen(
                            onBack = { rootNavController.popBackStack() },
                            viewModel = sharedViewModel
                        )
                    }
                }
            }
        }
    }
}