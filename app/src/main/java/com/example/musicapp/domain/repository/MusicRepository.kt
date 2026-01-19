package com.example.musicapp.domain.repository

import com.example.musicapp.data.local.db.FavoriteSongEntity
import com.example.musicapp.data.local.db.HistorySongEntity
import com.example.musicapp.data.local.db.PlaylistEntity
import com.example.musicapp.domain.model.Album
import com.example.musicapp.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    suspend fun getLocalSongs(): List<Song>

    suspend fun getLocalAlbums(): List<Album>

    suspend fun searchSongs(query: String): List<Song>

    fun getAllFavorites(): Flow<List<FavoriteSongEntity>>

    suspend fun insertFavorite(song: Song)

    suspend fun removeFavorite(songId: Long)

    fun isFavorite(songId: Long): Flow<Boolean>

    fun getHistory(): Flow<List<HistorySongEntity>>

    suspend fun addToHistory(song: Song)

    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    suspend fun createPlaylist(name: String)

    suspend fun deletePlaylist(playlistId: Long)

    fun getSongsByPlaylistId(playlistId: Long): Flow<List<Song>>

    suspend fun addSongToPlaylist(playlistId: Long, song: Song)

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)

    suspend fun getSongsByArtist(artistName: String): List<Song>

}