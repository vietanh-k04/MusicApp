package com.example.musicapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.musicapp.data.local.dao.MusicDao
import com.example.musicapp.data.local.db.*

@Database(
    entities = [
        FavoriteSongEntity::class,
        HistorySongEntity::class,
        PlaylistEntity::class,
        PlaylistSongEntity::class,
    ],
    version = 2,
    exportSchema = false
)

abstract class MusicDatabase : RoomDatabase() {
    abstract fun musicDao(): MusicDao
}