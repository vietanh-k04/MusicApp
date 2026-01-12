package com.example.musicapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ITunesSong(
    @SerializedName("trackName") val title: String?,
    @SerializedName("artistName") val artist: String?,
    @SerializedName("previewUrl") val musicUrl: String?,
    @SerializedName("artworkUrl100") val coverUrl: String?
)