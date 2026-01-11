package com.example.musicapp.domain.repository

import com.example.musicapp.domain.model.Song

interface MusicRepository {
    suspend fun getLocalSongs(): List<Song>
}