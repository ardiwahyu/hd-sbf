package com.bm.hdsbf.utils.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject lateinit var scheduler: ReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val serviceIntent = Intent(context, ReminderService::class.java)
        serviceIntent.putExtras(intent)
        ContextCompat.startForegroundService(context, serviceIntent)

        scheduler.startScheduler()
    }
}