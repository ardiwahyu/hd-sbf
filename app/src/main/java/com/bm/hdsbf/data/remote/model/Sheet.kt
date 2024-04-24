package com.bm.hdsbf.data.remote.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
class Sheet (
    @SerializedName("spreadsheetId") val spreadsheetId: String,
    @SerializedName("valueRanges") val valueRanges: List<ValueRange>
): Parcelable {

    @Parcelize
    class ValueRange(
        @SerializedName("range") val range: String,
        @SerializedName("majorDimension") val majorDimension: String,
        @SerializedName("values") val values: MutableList<List<String>>
    ): Parcelable
}