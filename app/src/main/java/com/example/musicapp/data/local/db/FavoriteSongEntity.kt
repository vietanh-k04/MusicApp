package com.example.musicapp.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_songs")
data class FavoriteSongEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val contentUri: String,
    val albumArtUri: String,
    val addedAt: Long = System.currentTimeMillis()
)