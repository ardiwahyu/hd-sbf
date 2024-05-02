package com.bm.hdsbf.utils.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bm.hdsbf.data.repository.schedule.ScheduleRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.collect

@HiltWorker
class UpdateScheduleWorker @AssistedInject constructor(
    private val scheduleRepository: ScheduleRepository,
    @Assisted private val context: Context,
    @Assisted workerParameters: WorkerParameters
): CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        scheduleRepository.getAllData().collect()
        return Result.success()
    }
}