package com.example.musicapp.data.local

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.example.musicapp.domain.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import androidx.core.net.toUri

class LocalMusicSource @Inject constructor(@ApplicationContext private val context: Context) {
    fun getAllSongs(): List<Song> {
        val songs = mutableListOf<Song>()

        // 1. Chọn nơi quét
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        // 2. Cột cần lấy
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION // Lấy thêm độ dài
        )

        // 3. BỘ LỌC QUAN TRỌNG (Sửa lại đoạn này)
        // Điều kiện: (IS_MUSIC != 0) VÀ (DURATION >= 30000 ms)
        // Ý nghĩa: Chỉ lấy file được đánh dấu là nhạc VÀ dài hơn 30 giây
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= ?"
        val selectionArgs = arrayOf("30000") // 30000 mili giây = 30 giây

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

                    // Xử lý tên tác giả nếu bị lỗi <unknown>
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
}