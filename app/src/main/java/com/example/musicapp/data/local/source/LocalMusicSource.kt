package com.example.musicapp.data.local.source

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.example.musicapp.domain.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import androidx.core.net.toUri
import com.example.musicapp.domain.model.Album

class LocalMusicSource @Inject constructor(@ApplicationContext private val context: Context) {
    fun getAllSongs(): List<Song> {
        val songs = mutableListOf<Song>()

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= ?"
        val selectionArgs = arrayOf("30000")

        try {
            val cursor = context.contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                "${MediaStore.Audio.Media.TITLE} ASC"
            )

            cursor?.use {
                val idCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumIdCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

                while (it.moveToNext()) {
                    val id = it.getLong(idCol)
                    val title = it.getString(titleCol) ?: "Unknown Song"
                    val artist = it.getString(artistCol) ?: "Unknown Artist"
                    val albumId = it.getLong(albumIdCol)

                    val finalArtist = if (artist == "<unknown>") "Unknown Artist" else artist

                    val contentUri = ContentUris.withAppendedId(collection, id)

                    val sArtworkUri = "content://media/external/audio/albumart".toUri()
                    val albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId).toString()

                    songs.add(Song(id, title, finalArtist, contentUri.toString(), albumArtUri))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return songs
    }

    fun getLocalAlbums(): List<Album> {
        val allSongs = getAllSongs()
        val groupedByArtist = allSongs.groupBy { it.artist ?: "Unknown Artist" }

        val virtualAlbums = groupedByArtist.map { (artistName, songs) ->
            val representativeArt = songs.firstOrNull()?.albumArtUri ?: ""
            val fakeId = artistName.hashCode().toLong()

            Album(
                id = fakeId,
                title = "Tuyển tập $artistName",
                artist = artistName,
                albumArtUri = representativeArt,
                numberOfSongs = songs.size
            )
        }

        return virtualAlbums.sortedBy { it.title }
    }

    fun getSongsByArtist(artistName: String): List<Song> {
        val allSongs = getAllSongs()
        return allSongs.filter { (it.artist ?: "Unknown Artist") == artistName }
    }
}