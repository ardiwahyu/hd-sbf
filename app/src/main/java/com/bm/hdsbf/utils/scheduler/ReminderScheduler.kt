package com.bm.hdsbf.utils.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private fun schedule(timeScheduler: TimeScheduler) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                return
            }
        }
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, timeScheduler.hour)
            set(Calendar.MINUTE, timeScheduler.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        val intent = Intent(context, ReminderReceiver::class.java)
        intent.putExtra("time", timeScheduler.name)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            timeScheduler.ordinal,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    fun startScheduler() {
        schedule(TimeScheduler.NOW)
        schedule(TimeScheduler.TOMORROW)
    }
}