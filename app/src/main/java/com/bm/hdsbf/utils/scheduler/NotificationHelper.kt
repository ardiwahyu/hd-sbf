package com.bm.hdsbf.utils.scheduler

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.bm.hdsbf.R
import com.bm.hdsbf.ui.schedule.ScheduleActivity

object NotificationHelper {

    fun createChannels(context: Context) {
        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val scheduleChannel = NotificationChannel(
            "schedule_reminder",
            "Pengingat Jadwal Helpdes",
            NotificationManager.IMPORTANCE_HIGH
        )
        val serviceChannel = NotificationChannel(
            "service_channel",
            "Background Service",
            NotificationManager.IMPORTANCE_LOW
        )
        manager.createNotificationChannel(scheduleChannel)
        manager.createNotificationChannel(serviceChannel)
    }

    fun createForegroundNotification(context: Context): Notification {
        return NotificationCompat.Builder(context, "service_channel")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle("Checking schedule")
            .setContentText("Memeriksa jadwal helpdesk...")
            .build()
    }

    fun showNotification(context: Context, dataIntent: Intent) {
        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, ScheduleActivity::class.java)
        intent.putExtra("is_from_notif", true)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val flag = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, flag)

        val notification = NotificationCompat.Builder(context, "schedule_reminder")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(dataIntent.getStringExtra("title"))
            .setContentText(dataIntent.getStringExtra("message"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}