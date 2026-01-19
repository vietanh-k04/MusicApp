package com.example.musicapp.utils

import android.annotation.SuppressLint
import java.util.concurrent.TimeUnit

@SuppressLint("DefaultLocale")
fun formatDuration(durationMs: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) -
            TimeUnit.MINUTES.toSeconds(minutes)
    return String.format("%02d:%02d", minutes, seconds)
}