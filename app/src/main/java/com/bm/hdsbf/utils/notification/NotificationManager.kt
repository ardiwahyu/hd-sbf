package com.bm.hdsbf.utils.notification

import android.content.Context
import android.icu.util.Calendar
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.bm.hdsbf.utils.ViewUtil.showShortToast
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.abs

class NotificationManager @Inject constructor(@ApplicationContext private val context: Context) {

    enum class Time { NOW, TOMORROW }

    fun activate() {
        scheduleTomorrow()
        scheduleNow()
        context.showShortToast("Pengingat Diaktifkan")
    }

    fun cancel() {
        WorkManager.getInstance(context).cancelAllWork()
        context.showShortToast("Pengingat Dimatikan")
    }

    private fun scheduleNow() {
        val alarmTime = Calendar.getInstance()
        alarmTime.set(Calendar.HOUR_OF_DAY, 3)
        alarmTime.set(Calendar.MINUTE, 0)
        alarmTime.set(Calendar.SECOND, 0)

        var longDelay = (alarmTime.timeInMillis - System.currentTimeMillis())
        if (longDelay < 0) longDelay = 24*60*60*1000 - abs(longDelay)
        val work = PeriodicWorkRequest.Builder(
            NotificationWorker::class.java, 1, TimeUnit.DAYS
        ).setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setInputData(Data.Builder().putString("time", Time.NOW.name).build())
            .setInitialDelay(longDelay, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueue(work)
    }

    private fun scheduleTomorrow() {
        val alarmTime = Calendar.getInstance()
        alarmTime.set(Calendar.HOUR_OF_DAY, 18)
        alarmTime.set(Calendar.MINUTE, 0)
        alarmTime.set(Calendar.SECOND, 0)

        var longDelay = (alarmTime.timeInMillis - System.currentTimeMillis())
        if (longDelay < 0) longDelay = 24*60*60*1000 - abs(longDelay)
        val work = PeriodicWorkRequest.Builder(
            NotificationWorker::class.java, 1, TimeUnit.DAYS
        ).setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setInputData(Data.Builder().putString("time", Time.TOMORROW.name).build())
            .setInitialDelay(longDelay, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueue(work)
    }
}