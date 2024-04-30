package com.bm.hdsbf.data.remote.service

import com.bm.hdsbf.BuildConfig
import com.google.gson.JsonObject
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

interface GoogleDriveService {
    @GET("files/{fileId}")
    suspend fun getLastModifiedSchedule(
        @Path("fileId") id: String = BuildConfig.SPREADSHEET_ID,
        @Query("key") key: String = BuildConfig.API_KEY,
        @Query("fields") fields: String = "modifiedTime"
    ): Response<JsonObject>

    @GET("files/{fileId}")
    suspend fun getLastModifiedApp(
        @Path("fileId") id: String = BuildConfig.APP_ID,
        @Query("key") key: String = BuildConfig.API_KEY,
        @Query("fields") fields: String = "name"
    ): Response<JsonObject>

    @Streaming
    @GET("files/{fileId}")
    suspend fun downloadFile(
        @Path("fileId") id: String = BuildConfig.APP_ID,
        @Query("key") key: String = BuildConfig.API_KEY,
        @Query("alt") alt: String = "media"
    ): ResponseBody
}