package com.bm.hdsbf.data.remote.service

import com.bm.hdsbf.data.remote.model.AbsensiResult
import com.bm.hdsbf.data.remote.model.LokasiAbsensi
import com.bm.hdsbf.data.remote.model.UserAbsensi
import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

interface AbsensiService {

    @POST("auth/login")
    suspend fun login(
        @Body body: JsonObject
    ): Response<AbsensiResult<UserAbsensi>>

    @GET("attendances")
    suspend fun getConfig(
        @Header("device-id") deviceId: String,
        @Header("Cookie") cookie: String
    ): Response<AbsensiResult<LokasiAbsensi>>

    @PUT("attendances")
    suspend fun absen(
        @Body body: JsonObject,
        @Header("device-id") deviceId: String,
        @Header("Cookie") cookie: String
    ): Response<JsonObject>
}