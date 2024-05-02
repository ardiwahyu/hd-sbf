package com.bm.hdsbf.data.repository.worker

import com.bm.hdsbf.data.local.db.dao.ScheduleDao
import com.bm.hdsbf.data.local.db.entities.ScheduleVo
import com.bm.hdsbf.data.local.sp.PreferenceClass
import com.bm.hdsbf.utils.CalendarUtil.displayName
import com.kizitonwose.calendar.core.yearMonth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.time.LocalDate
import javax.inject.Inject

class WorkerRepository @Inject constructor(
    private val scheduleDao: ScheduleDao,
    private val preferenceClass: PreferenceClass
){

    fun checkSchedule(date: LocalDate): Flow<ScheduleVo?> {
        return flow {
            try {
                val day = date.dayOfMonth
                val month = date.yearMonth.displayName()
                val name = preferenceClass.getName()!!
                val listSchedule = scheduleDao.getScheduleByDateAndName(month, day, name)
                if (listSchedule.isNotEmpty()) {
                    emit(listSchedule.first())
                } else emit(null)
            } catch (e: Exception) {
                emit(null)
            }
        }.flowOn(Dispatchers.IO)
    }
}