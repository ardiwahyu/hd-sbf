package com.bm.hdsbf.data.repository.absensi

import android.os.Build
import com.bm.hdsbf.data.local.sp.PreferenceClass
import com.bm.hdsbf.data.remote.Resource
import com.bm.hdsbf.data.remote.model.LokasiAbsensi
import com.bm.hdsbf.data.remote.model.UserAbsensi
import com.bm.hdsbf.data.remote.service.AbsensiService
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class AbsensiRepository @Inject constructor(
    private val absensiService: AbsensiService,
    private val preferenceClass: PreferenceClass
) {

    fun login(nik: String, pass: String, deviceId: String): Flow<Resource<UserAbsensi?>> {
        return flow {
            emit(Resource.OnLoading(true))
            preferenceClass.setIdDevice(deviceId)
            val body = JsonObject().apply {
                addProperty("username", nik)
                addProperty("password", pass)
                addProperty("device_id", deviceId)
                addProperty("device_name", "${Build.MANUFACTURER} ${Build.PRODUCT}")
                addProperty("device_timezone", "Asia/Jakarta")
            }
            try {
                val response = absensiService.login(body)
                if (response.isSuccessful) {
                    if (response.body()?.errors == null) {
                        preferenceClass.setString("username", nik)
                        preferenceClass.setString("password", pass)
                        response.headers()["Set-Cookie"]?.split(";")?.first()?.let {
                            preferenceClass.setString("cookie", it)
                        }
                        emit(Resource.OnSuccess(response.body()?.result))
                    } else {
                        emit(Resource.OnError(response.body()?.message.toString()))
                    }
                } else {
                    emit(Resource.OnError(response.errorBody()?.string().toString()))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emit(Resource.OnError("error: ${e.localizedMessage}"))
            } finally {
                emit(Resource.OnLoading(false))
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getConfig(): Flow<Resource<LokasiAbsensi?>> {
        return flow {
            emit(Resource.OnLoading(true))
            try {
                val response = absensiService.getConfig(
                    preferenceClass.getIdDevice()!!,
                    preferenceClass.getString("cookie")!!
                )
                if (response.isSuccessful) {
                    if (response.body()?.errors == null) {
                        emit(Resource.OnSuccess(response.body()?.result))
                    } else {
                        emit(Resource.OnError(response.body()?.message.toString()))
                    }
                } else {
                    emit(Resource.OnError(response.errorBody()?.string().toString()))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emit(Resource.OnError("error: ${e.localizedMessage}"))
            } finally {
                emit(Resource.OnLoading(false))
            }
        }.flowOn(Dispatchers.IO)
    }

    fun absen(idLoc: Long, lat: Double, long: Double, type: String): Flow<Resource<String>> {
        return flow {
            emit(Resource.OnLoading(true))
            val body = JsonObject().apply {
                addProperty("location_id", idLoc)
                addProperty("latitude", lat)
                addProperty("longitude", long)
                addProperty("type", type)
            }
            try {
                val response = absensiService.absen(
                    body,
                    preferenceClass.getIdDevice()!!,
                    preferenceClass.getString("cookie")!!
                )
                if (response.isSuccessful) {
                    try {
                        val jsonObject = response.body()
                        if (jsonObject?.has("message") == true) {
                            emit(Resource.OnSuccess(jsonObject.get("message").asString))
                        } else {
                            emit(Resource.OnError("Gagal"))
                        }
                    } catch (e: Exception) {
                        emit(Resource.OnError("Error: ${e.localizedMessage}"))
                    }
                } else {
                    emit(Resource.OnError(response.errorBody()?.string().toString()))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emit(Resource.OnError("error: ${e.localizedMessage}"))
            } finally {
                emit(Resource.OnLoading(false))
            }
        }.flowOn(Dispatchers.IO)
    }
}