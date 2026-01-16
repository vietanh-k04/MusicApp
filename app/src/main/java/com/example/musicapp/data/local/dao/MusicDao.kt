package com.example.musicapp.data.local.dao

import androidx.room.*
import com.example.musicapp.data.local.db.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {
    @Query("SELECT * FROM favorite_songs ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteSongEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(song: FavoriteSongEntity)

    @Delete
    suspend fun removeFavorite(song: FavoriteSongEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_songs WHERE id = :id)")
    fun isFavorite(id: Long): Flow<Boolean>

    @Query("SELECT * FROM history_songs ORDER BY listenedAt DESC LIMIT 50")
    fun getHistory(): Flow<List<HistorySongEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(song: HistorySongEntity)

    @Insert
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSongToPlaylist(crossRef: PlaylistSongCrossRef)
}