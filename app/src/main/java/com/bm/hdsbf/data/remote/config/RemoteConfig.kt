package com.bm.hdsbf.data.remote.config

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import javax.inject.Inject

class RemoteConfig @Inject constructor() {
    private var firebaseRemoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig

    fun getAppId() = firebaseRemoteConfig.getString("app_id")
    fun getSpreadsheetId() = firebaseRemoteConfig.getString("spreadsheet_id")
    fun getRangeSchedule() = firebaseRemoteConfig.getString("range_schedule")
    fun getRangeOff() = firebaseRemoteConfig.getString("range_off")
    fun getRangeTime() = firebaseRemoteConfig.getString("range_time")
}