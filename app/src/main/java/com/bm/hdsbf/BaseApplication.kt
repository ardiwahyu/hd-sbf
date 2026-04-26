package com.bm.hdsbf

import android.app.Application
import com.bm.hdsbf.utils.scheduler.NotificationHelper
import com.bm.hdsbf.utils.scheduler.ReminderScheduler
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class BaseApplication: Application() {
    @Inject lateinit var scheduler: ReminderScheduler

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)

        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSetting = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 60 * 60
            fetchTimeoutInSeconds = 5
        }
        remoteConfig.setConfigSettingsAsync(configSetting)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        NotificationHelper.createChannels(this)
        scheduler.startScheduler()
    }
}