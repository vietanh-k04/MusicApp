package com.example.musicapp.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_songs")
data class HistorySongEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val contentUri: String,
    val albumArtUri: String,
    val listenedAt: Long = System.currentTimeMillis()
)
