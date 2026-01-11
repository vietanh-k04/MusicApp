package com.example.musicapp.domain.model

data class Song(
    val id: Long?,
    val title: String?,
    val artist: String?,
    val contentUri: String?,
    val albumArtUri: String?
)