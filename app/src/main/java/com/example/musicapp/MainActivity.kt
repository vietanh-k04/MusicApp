package com.example.musicapp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 1. Khởi tạo ViewModel sớm để lấy config Theme
            val sharedViewModel: SharedViewModel = hiltViewModel()

            // 2. Lắng nghe trạng thái Theme
            val isDarkTheme by sharedViewModel.isDarkTheme.collectAsState()

            // 3. Truyền trạng thái vào MusicAppTheme
            MusicAppTheme(darkTheme = isDarkTheme) {

                val rootNavController = rememberNavController()

                NavHost(
                    navController = rootNavController,
                    startDestination = "main"
                ) {
                    composable("main") {
                        MainScreen(
                            sharedViewModel = sharedViewModel,
                            onFullScreenPlayerRequest = {
                                rootNavController.navigate("player")
                            }
                        )
                    }

                    composable(
                        route = "player",
                        enterTransition = {
                            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(400))
                        },
                        exitTransition = {
                            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(400))
                        },
                        popEnterTransition = { null },
                        popExitTransition = {
                            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(400))
                        }
                    ) {
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