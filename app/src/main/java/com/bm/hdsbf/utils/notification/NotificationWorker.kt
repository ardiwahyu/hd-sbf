package com.bm.hdsbf.utils.notification

import android.content.Context
import android.content.Intent
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.bm.hdsbf.data.repository.schedule.ScheduleRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

@HiltWorker
class NotificationWorker @AssistedInject constructor(
    private val scheduleRepository: ScheduleRepository,
    @Assisted private val context: Context,
    @Assisted workerParameters: WorkerParameters
): Worker(context, workerParameters) {

    override fun doWork(): Result {
        val time = inputData.getString("time")
        var localDate = LocalDate.now()
        if (time == NotificationManager.Time.TOMORROW.name) {
            localDate = localDate.plusDays(1)
        }
        CoroutineScope(Dispatchers.Main).launch {
            scheduleRepository.checkSchedule(localDate).collect {
                if (it != null) {
                    val title = if (time == NotificationManager.Time.NOW.name) {
                        "Hari ini kamu Helpdesk"
                    } else { "Besok kamu Helpdesk" }
                    val message = "${it.type} ${it.name} tanggal ${it.date} ${it.month}"
                    val intent = Intent()
                    intent.putExtra("title", title)
                    intent.putExtra("message", message)
                    NotificationHelper.showNotification(context, intent)
                }
            }
        }
        return Result.success()
    }
}