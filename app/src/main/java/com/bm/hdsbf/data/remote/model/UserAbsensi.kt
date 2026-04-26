package com.bm.hdsbf.data.remote.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
class UserAbsensi (
    @SerializedName("id") val id: Long,
    @SerializedName("employee") val employee: Employee
): Parcelable {

    @Parcelize
    class Employee(
        @SerializedName("name") val name: String,
        @SerializedName("number") val number: String,
        @SerializedName("position") val position: String
    ): Parcelable
}