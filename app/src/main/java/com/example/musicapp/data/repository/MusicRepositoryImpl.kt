package com.example.musicapp.data.repository

import com.example.musicapp.data.local.LocalMusicSource
import com.example.musicapp.domain.model.Song
import com.example.musicapp.domain.repository.MusicRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MusicRepositoryImpl @Inject constructor(private val localMusicSource: LocalMusicSource) :
    MusicRepository {

    // Chuyển sang IO Thread để không làm đơ giao diện khi quét nhạc
    override suspend fun getLocalSongs(): List<Song> = withContext(Dispatchers.IO) {
        localMusicSource.getAllSongs()
    }
}