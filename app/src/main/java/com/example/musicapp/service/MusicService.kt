package com.example.musicapp.service

import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MusicService : MediaSessionService() {

    @Inject
    lateinit var player: ExoPlayer

    private var mediaSession: MediaSession? = null

    // Khai báo biến Receiver
    private var headsetReceiver: HeadsetReceiver? = null

    override fun onCreate() {
        super.onCreate()
        mediaSession = MediaSession.Builder(this, player).build()

        // --- BẮT ĐẦU ĐOẠN CODE BROADCAST RECEIVER ---

        // 1. Khởi tạo Receiver
        headsetReceiver = HeadsetReceiver(player)

        // 2. Tạo bộ lọc (Chỉ nghe 2 sự kiện: Cắm rút tai nghe & Bị ồn)
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_HEADSET_PLUG)
            addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        }

        // 3. Đăng ký với hệ thống
        registerReceiver(headsetReceiver, filter)

        // --- KẾT THÚC ĐOẠN CODE BROADCAST RECEIVER ---
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }

        // --- QUAN TRỌNG: Hủy đăng ký Receiver khi Service chết để tránh lỗi ---
        headsetReceiver?.let {
            unregisterReceiver(it)
        }

        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player?.playWhenReady == false || player?.mediaItemCount == 0) {
            stopSelf()
        }
    }
}