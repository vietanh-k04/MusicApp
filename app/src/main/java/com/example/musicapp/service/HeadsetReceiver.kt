package com.example.musicapp.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.util.Log
import androidx.media3.exoplayer.ExoPlayer

// Nhận vào player để điều khiển
class HeadsetReceiver(private val player: ExoPlayer) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_HEADSET_PLUG) {
            val state = intent.getIntExtra("state", -1)

            when (state) {
                0 -> {
                    // State = 0: Tai nghe bị RÚT ra
                    Log.d("MusicAppReceiver", "Tai nghe đã rút -> Dừng nhạc")
                    if (player.isPlaying) {
                        player.pause()
                    }
                }
                1 -> {
                    // State = 1: Tai nghe được CẮM vào
                    Log.d("MusicAppReceiver", "Tai nghe đã cắm -> Tự động phát")
                    // Tính năng "Hay": Nếu đang có bài hát (đã chuẩn bị) thì tự phát luôn
                    if (!player.isPlaying && player.playbackState == ExoPlayer.STATE_READY) {
                        player.play()
                    }
                }
            }
        }

        // Sự kiện phụ: Khi âm thanh bị ồn (ví dụ rút dây loa) - Hệ thống Android bắn cái này
        if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
            Log.d("MusicAppReceiver", "Ồn ào quá -> Dừng nhạc ngay")
            if (player.isPlaying) {
                player.pause()
            }
        }
    }
}