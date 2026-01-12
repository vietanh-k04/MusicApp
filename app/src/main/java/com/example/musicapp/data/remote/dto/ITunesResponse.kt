package com.example.musicapp.data.remote.dto

data class ITunesResponse(
    val resultCount: Int,
    val results: List<ITunesSong>
)
