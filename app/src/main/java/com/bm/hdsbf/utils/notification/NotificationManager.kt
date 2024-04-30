package com.bm.hdsbf.utils.notification

import android.content.Context
import android.icu.util.Calendar
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
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
        val worker = PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.DAYS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setInputData(workDataOf("time" to Time.NOW.name))
            .setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.MINUTES)
            .setInitialDelay(longDelay, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueue(worker)
    }

    private fun scheduleTomorrow() {
        val alarmTime = Calendar.getInstance()
        alarmTime.set(Calendar.HOUR_OF_DAY, 11)
        alarmTime.set(Calendar.MINUTE, 24)
        alarmTime.set(Calendar.SECOND, 0)

        var longDelay = (alarmTime.timeInMillis - System.currentTimeMillis())
        if (longDelay < 0) longDelay = 24*60*60*1000 - abs(longDelay)
        val worker = PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.DAYS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setInputData(workDataOf("time" to Time.TOMORROW.name))
            .setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.MINUTES)
            .setInitialDelay(longDelay, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueue(worker)
    }
}