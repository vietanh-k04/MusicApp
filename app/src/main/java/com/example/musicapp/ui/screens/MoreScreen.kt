package com.example.musicapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.musicapp.ui.viewmodel.SharedViewModel
import com.example.musicapp.R
import com.example.musicapp.ui.viewmodel.SettingsViewModel
import com.example.musicapp.utils.formatDuration

@Composable
fun MoreScreen(sharedViewModel: SharedViewModel, settingsViewModel: SettingsViewModel) {
    val remainingTime by sharedViewModel.remainingTime.collectAsState()

    val isDarkTheme by settingsViewModel.isDarkTheme.collectAsState()

    var showTimerDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    var showLangDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 1. HEADER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF2193b0), Color(0xFF6dd5ed))
                    )
                ),
            contentAlignment = Alignment.BottomStart
        ) {
            Text(
                text = stringResource(R.string.more_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingItem(
            icon = Icons.Default.AccessTime,
            title = stringResource(R.string.more_timer),
            subtitle = if (remainingTime != null) "Time: ${formatDuration(remainingTime!!)}" else stringResource(R.string.more_timer_desc),
            iconColor = Color(0xFFFF9800),
            onClick = { showTimerDialog = true }
        )

        // Giao diện
        SettingItem(
            icon = Icons.Default.DarkMode,
            title = stringResource(R.string.more_theme),
            subtitle = stringResource(R.string.more_theme_desc),
            iconColor = Color(0xFF9C27B0),
            onClick = {
                settingsViewModel.setDarkTheme(!isDarkTheme)
            },
            trailing = {
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { isChecked ->
                        settingsViewModel.setDarkTheme(isChecked)
                    }
                )
            }
        )

        SettingItem(
            icon = Icons.Default.Language,
            title = stringResource(R.string.more_lang),
            subtitle = stringResource(R.string.more_lang_desc),
            iconColor = Color(0xFFE91E63),
            onClick = { showLangDialog = true }
        )

        // Thông tin
        SettingItem(
            icon = Icons.Default.Info,
            title = stringResource(R.string.more_info),
            subtitle = stringResource(R.string.more_ver),
            iconColor = Color(0xFF2196F3),
            onClick = { showAboutDialog = true }
        )
    }

    // Dialog Hẹn giờ
    if (showTimerDialog) {
        AlertDialog(
            onDismissRequest = { showTimerDialog = false },
            title = { Text("Hẹn giờ tắt nhạc") },
            text = {
                Column {
                    TimerOption(15) { min -> sharedViewModel.setSleepTimer(min); showTimerDialog = false }
                    TimerOption(30) { min -> sharedViewModel.setSleepTimer(min); showTimerDialog = false }
                    TimerOption(60) { min -> sharedViewModel.setSleepTimer(min); showTimerDialog = false }

                    if (remainingTime != null) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            thickness = DividerDefaults.Thickness,
                            color = DividerDefaults.color
                        )
                        TextButton(
                            onClick = {
                                sharedViewModel.cancelSleepTimer()
                                showTimerDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Hủy hẹn giờ", color = Color.Red)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showTimerDialog = false }) { Text("Đóng") }
            }
        )
    }

    // Dialog Thông tin
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            icon = { Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(40.dp)) },
            title = { Text(stringResource(R.string.app_name)) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.more_ver), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.more_dev), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(R.string.more_reserved), fontSize = 10.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showLangDialog) {
        AlertDialog(
            onDismissRequest = { showLangDialog = false },
            title = { Text(stringResource(R.string.more_lang)) },
            text = {
                Column {
                    // Chọn Tiếng Việt
                    TextButton(
                        onClick = {
                            settingsViewModel.setLanguage("vi")
                            showLangDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Tiếng Việt 🇻🇳", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                    }

                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

                    TextButton(
                        onClick = {
                            settingsViewModel.setLanguage("en")
                            showLangDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("English 🇺🇸", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLangDialog = false }) {
                    Text(stringResource(R.string.close))
                }
            }
        )
    }
}

@Composable
fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color,
    onClick: () -> Unit,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        if (trailing != null) trailing() else Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
    }
}

@Composable
fun TimerOption(minutes: Int, onClick: (Int) -> Unit) {
    TextButton(
        onClick = { onClick(minutes) },
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(12.dp)
    ) {
        Text("$minutes ${stringResource(R.string.minutes)}", fontSize = 16.sp)
    }
}