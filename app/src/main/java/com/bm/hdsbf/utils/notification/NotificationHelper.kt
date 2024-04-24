package com.bm.hdsbf.utils.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import com.bm.hdsbf.R

object NotificationHelper {
    private const val CHANNEL_ID = "SBF_HD_CHANNEL_ID"
    private const val CHANNEL_NAME = "SBF_HD_CHANNEL_NAME"

    fun showNotification(context: Context, intent: Intent) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID, CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)
        val builder = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(intent.getStringExtra("title"))
            .setContentText(intent.getStringExtra("message"))
            .setAutoCancel(false)
        val notification = builder.build()
        notificationManager.notify(0, notification)
    }
}