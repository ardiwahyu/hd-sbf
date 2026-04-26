package com.bm.hdsbf.utils.scheduler

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import com.bm.hdsbf.data.local.sp.PreferenceClass
import com.bm.hdsbf.data.repository.worker.WorkerRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class ReminderService : Service() {

    @Inject lateinit var workerRepository: WorkerRepository
    @Inject lateinit var preferenceClass: PreferenceClass

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                1,
                NotificationHelper.createForegroundNotification(this),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(
                1,
                NotificationHelper.createForegroundNotification(this)
            )
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceScope.launch {
            val time = intent?.getStringExtra("time")
            var localDate = LocalDate.now()
            if (time == TimeScheduler.TOMORROW.name) {
                localDate = localDate.plusDays(1)
            }
            if (preferenceClass.getReminder()) {
                workerRepository.checkSchedule(localDate).collect {
                    if (it != null) {
                        val title = if (time == TimeScheduler.NOW.name) {
                            "Hari ini kamu Helpdesk"
                        } else { "Besok kamu Helpdesk" }
                        val message = "${it.type} ${it.name} tanggal ${it.date} ${it.month}"
                        val notificationIntent = Intent()
                        notificationIntent.putExtra("title", title)
                        notificationIntent.putExtra("message", message)
                        NotificationHelper.showNotification(this@ReminderService, notificationIntent)
                    }
                }
            }

            stopSelf(startId)
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?) = null
}