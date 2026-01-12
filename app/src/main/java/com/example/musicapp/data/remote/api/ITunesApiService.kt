package com.example.musicapp.data.remote.api

import com.example.musicapp.data.remote.dto.ITunesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ITunesApiService {
    @GET("search")
    suspend fun searchSongs(
        @Query("term") query: String,
        @Query("media") media: String = "music",
        @Query("limit") limit: Int = 20
    ): Response<ITunesResponse>
}