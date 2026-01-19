package com.example.musicapp.data.repository

import com.example.musicapp.data.local.dao.MusicDao
import com.example.musicapp.data.local.db.FavoriteSongEntity
import com.example.musicapp.data.local.db.HistorySongEntity
import com.example.musicapp.data.local.db.PlaylistEntity
import com.example.musicapp.data.local.db.PlaylistSongEntity
import com.example.musicapp.data.local.source.LocalMusicSource
import com.example.musicapp.data.remote.api.ITunesApiService
import com.example.musicapp.domain.model.Album
import com.example.musicapp.domain.model.Song
import com.example.musicapp.domain.repository.MusicRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MusicRepositoryImpl @Inject constructor(private val localMusicSource: LocalMusicSource, private val apiService: ITunesApiService, private val musicDao: MusicDao) :
    MusicRepository {

    override suspend fun getLocalSongs(): List<Song> = withContext(Dispatchers.IO) {
        localMusicSource.getAllSongs()
    }

    override suspend fun getLocalAlbums(): List<Album> = withContext(Dispatchers.IO) {
        localMusicSource.getLocalAlbums()
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

    override fun getAllFavorites(): Flow<List<FavoriteSongEntity>> = musicDao.getAllFavorites()

    override suspend fun insertFavorite(song: Song) {
        musicDao.insertFavorite(
            FavoriteSongEntity(song.id ?: 0, song.title?:"", song.artist?:"", song.contentUri?:"", song.albumArtUri?:"")
        )
    }

    override suspend fun removeFavorite(songId: Long) {
        musicDao.deleteFavoriteById(songId)
    }

    override fun isFavorite(songId: Long): Flow<Boolean> = musicDao.isFavorite(songId)

    override fun getHistory(): Flow<List<HistorySongEntity>> = musicDao.getHistory()

    override suspend fun addToHistory(song: Song) {
        musicDao.insertHistory(
            HistorySongEntity(song.id ?: 0, song.title?:"", song.artist?:"", song.contentUri?:"", song.albumArtUri?:"")
        )
    }

    override fun getAllPlaylists(): Flow<List<PlaylistEntity>> = musicDao.getAllPlaylists()

    override suspend fun createPlaylist(name: String) {
        musicDao.insertPlaylist(PlaylistEntity(playlistName = name))
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        musicDao.deletePlaylist(playlistId)
    }

    override fun getSongsByPlaylistId(playlistId: Long): Flow<List<Song>> {
        return musicDao.getSongsByPlaylistId(playlistId).map { entities ->
            entities.map { entity ->
                Song(entity.songId, entity.title, entity.artist, entity.contentUri, entity.albumArtUri)
            }
        }
    }

    override suspend fun addSongToPlaylist(playlistId: Long, song: Song) {
        musicDao.insertSongToPlaylist(
            PlaylistSongEntity(
                playlistId = playlistId,
                songId = song.id ?: 0,
                title = song.title ?: "",
                artist = song.artist ?: "",
                contentUri = song.contentUri ?: "",
                albumArtUri = song.albumArtUri ?: ""
            )
        )
    }

    override suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        musicDao.removeSongFromPlaylist(playlistId, songId)
    }

    override suspend fun getSongsByArtist(artistName: String): List<Song> = localMusicSource.getSongsByArtist(artistName)
}