package com.bm.hdsbf.data.remote.service

import com.bm.hdsbf.BuildConfig
import com.bm.hdsbf.data.remote.model.Sheet
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GoogleSheetService {
    @GET("spreadsheets/{id}/values:batchGet")
    suspend fun getData(
        @Path("id") id: String,
        @Query("key") key: String = BuildConfig.API_KEY,
        @Query("ranges") rangesSchedule: String,
        @Query("ranges") rangesOff: String,
        @Query("ranges") rangesTime: String,
        @Query("majorDimension") majorDimension: String = "ROWS",
        @Query("valueRenderOption") valueRenderOption: String = "FORMATTED_VALUE",
        @Query("dateTimeRenderOption") dateTimeRenderOption: String = "FORMATTED_STRING"
    ): Response<Sheet>
}