package com.example.musicapp.ui.player

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
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
    // 1. Lấy các State từ ViewModel
    val currentSong by viewModel.currentPlayingSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPos by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val shuffleMode by viewModel.shuffleMode.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()

    // 2. Logic Animation xoay ảnh đĩa nhạc
    val infiniteTransition = rememberInfiniteTransition(label = "rotate")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing) // Xoay 1 vòng trong 10 giây
        ),
        label = "rotation"
    )
    // Nếu đang phát thì lấy góc xoay, nếu dừng thì giữ nguyên (hoặc về 0 tuỳ logic, ở đây ta để 0 cho đơn giản)
    // Lưu ý: Để dừng xoay tại chỗ cần logic phức tạp hơn, tạm thời ta dùng logic: Nhạc chạy -> Xoay.
    val rotationState = if (isPlaying) angle else 0f

    // 3. Nếu không có bài hát nào (trường hợp hiếm), quay về
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
        // --- HEADER: NÚT BACK ---
        Row(modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Back", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- ẢNH BÌA (ALBUM ART) ---
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(song.albumArtUri)
                .crossfade(true)
                .build(),
            contentDescription = "Album Art",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(300.dp)
                .rotate(rotationState) // <--- SỬ DỤNG BIẾN rotationState TẠI ĐÂY
                .clip(CircleShape)     // Bo tròn thành đĩa nhạc
                .background(Color.DarkGray, CircleShape) // Viền nền
                .padding(2.dp)
        )

        Spacer(modifier = Modifier.height(30.dp))

        // --- TÊN BÀI HÁT & CA SĨ ---
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

        Spacer(modifier = Modifier.weight(1f))

        // --- THANH THỜI GIAN (SLIDER) ---
        // Đặt ở trên các nút điều khiển (Chuẩn UX)
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

        // --- CÁC NÚT ĐIỀU KHIỂN ---
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

            // 3. Play/Pause (To nhất ở giữa)
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