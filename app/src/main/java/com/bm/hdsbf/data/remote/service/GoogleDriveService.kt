package com.bm.hdsbf.data.remote.service

import com.bm.hdsbf.BuildConfig
import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GoogleDriveService {
    @GET("files/{fileId}")
    suspend fun getLastModifiedSchedule(
        @Path("fileId") id: String,
        @Query("key") key: String = BuildConfig.API_KEY,
        @Query("fields") fields: String = "modifiedTime"
    ): Response<JsonObject>

    @GET("files/{fileId}")
    suspend fun getLastModifiedApp(
        @Path("fileId") id: String,
        @Query("key") key: String = BuildConfig.API_KEY,
        @Query("fields") fields: String = "name"
    ): Response<JsonObject>
}