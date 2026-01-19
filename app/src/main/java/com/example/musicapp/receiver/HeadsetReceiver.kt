package com.example.musicapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import androidx.media3.exoplayer.ExoPlayer

class HeadsetReceiver(private val player: ExoPlayer) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_HEADSET_PLUG) {
            val state = intent.getIntExtra("state", -1)

            when (state) {
                0 -> {
                    if (player.isPlaying) {
                        player.pause()
                    }
                }
            }
        }

        // Sự kiện phụ: Khi âm thanh bị ồn (ví dụ rút dây loa) - Hệ thống Android bắn cái này
        if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
            if (player.isPlaying) {
                player.pause()
            }
        }
    }
}