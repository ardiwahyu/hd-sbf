package com.bm.hdsbf.data.remote.model

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

class AbsensiResult<T> (
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: T?,
    @SerializedName("errors") val errors: JsonObject?
)