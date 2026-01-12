package com.example.musicapp.data.repository

import com.example.musicapp.data.local.LocalMusicSource
import com.example.musicapp.data.remote.api.ITunesApiService
import com.example.musicapp.domain.model.Song
import com.example.musicapp.domain.repository.MusicRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MusicRepositoryImpl @Inject constructor(private val localMusicSource: LocalMusicSource, private val apiService: ITunesApiService) :
    MusicRepository {

    // Chuyển sang IO Thread để không làm đơ giao diện khi quét nhạc
    override suspend fun getLocalSongs(): List<Song> = withContext(Dispatchers.IO) {
        localMusicSource.getAllSongs()
    }

    override suspend fun searchSongs(query: String): List<Song> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.searchSongs(query = query)

            if (response.isSuccessful && response.body() != null) {
                // Mapping: Biến đổi dữ liệu từ API thành Song của app
                response.body()!!.results.map { dto ->
                    Song(
                        id = dto.musicUrl.hashCode().toLong(), // Tạm dùng hashcode làm ID
                        title = dto.title ?: "Unknown",
                        artist = dto.artist ?: "Unknown",
                        contentUri = dto.musicUrl ?: "", // Link nhạc online (preview 30s)
                        albumArtUri = dto.coverUrl ?: "" // Link ảnh bìa
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}