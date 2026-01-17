package com.example.musicapp.ui.player

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.musicapp.ui.viewmodel.SharedViewModel
import com.example.musicapp.utils.formatDuration

@Composable
fun PlayerScreen(onBack: () -> Unit, viewModel: SharedViewModel) {
    val currentSong by viewModel.currentPlayingSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPos by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val shuffleMode by viewModel.shuffleMode.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()

    // Logic Animation xoay ảnh đĩa nhạc
    val infiniteTransition = rememberInfiniteTransition(label = "rotate")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing)
        ),
        label = "rotation"
    )
    // Nếu đang phát thì xoay, dừng thì đứng im (hoặc về 0)
    val rotationState = if (isPlaying) angle else 0f

    // 3. Nếu không có bài hát nào, quay về
    if (currentSong == null) {
        onBack()
        return
    }

    val song = currentSong!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1E1E1E), Color(0xFF000000))
                )
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Back", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(song.albumArtUri)
                .crossfade(true)
                .build(),
            contentDescription = "Album Art",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(300.dp)
                .rotate(rotationState)
                .clip(CircleShape)
                .background(Color.DarkGray, CircleShape)
                .padding(2.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title ?: "Unknown",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist ?: "Unknown",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = { viewModel.toggleFavorite() }) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (isFavorite) Color.Red else Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(modifier = Modifier.fillMaxWidth()) {
            Slider(
                value = currentPos.toFloat(),
                onValueChange = { newPos ->
                    viewModel.seekTo(newPos.toLong())
                },
                valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.Gray.copy(alpha = 0.5f)
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = formatDuration(currentPos), color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                Text(text = formatDuration(duration), color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Shuffle
            IconButton(onClick = { viewModel.toggleShuffle() }) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (shuffleMode) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                    modifier = Modifier.size(28.dp)
                )
            }

            // 2. Previous
            IconButton(onClick = { viewModel.skipToPrevious() }, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Prev", tint = Color.White, modifier = Modifier.size(40.dp))
            }

            // 3. Play/Pause
            IconButton(
                onClick = { viewModel.toggleMusic() },
                modifier = Modifier
                    .size(72.dp)
                    .background(Color.White, CircleShape)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.Black,
                    modifier = Modifier.size(40.dp)
                )
            }

            // 4. Next
            IconButton(onClick = { viewModel.skipToNext() }, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(40.dp))
            }

            // 5. Repeat
            IconButton(onClick = { viewModel.toggleRepeat() }) {
                val icon = if (repeatMode == Player.REPEAT_MODE_ONE) Icons.Default.RepeatOne else Icons.Default.Repeat
                val tint = if (repeatMode == Player.REPEAT_MODE_OFF) Color.Gray.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary

                Icon(
                    imageVector = icon,
                    contentDescription = "Repeat",
                    tint = tint,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}