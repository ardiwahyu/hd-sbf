package com.bm.hdsbf.data.remote.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
class LokasiAbsensi (
    @SerializedName("locations") val location: List<Location>,
    @SerializedName("start") val start: String,
    @SerializedName("end") val end: String
): Parcelable {

    @Parcelize
    class Location (
        @SerializedName("id") val id: Long,
        @SerializedName("code") val code: String,
        @SerializedName("longitude") val longitude: Double,
        @SerializedName("latitude") val latitude: Double,
        @SerializedName("radius") val radius: Double
    ): Parcelable
}