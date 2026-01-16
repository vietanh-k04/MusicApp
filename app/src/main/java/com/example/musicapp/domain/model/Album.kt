package com.example.musicapp.domain.model

data class Album(
    val id: Long?,
    val title: String?,
    val artist: String?,
    val albumArtUri: String?,
    val numberOfSongs: Int?
)