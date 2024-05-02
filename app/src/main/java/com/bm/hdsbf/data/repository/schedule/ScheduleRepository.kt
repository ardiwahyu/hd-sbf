package com.bm.hdsbf.data.repository.schedule

import com.bm.hdsbf.data.local.db.dao.ScheduleDao
import com.bm.hdsbf.data.local.db.entities.ScheduleVo
import com.bm.hdsbf.data.local.sp.PreferenceClass
import com.bm.hdsbf.data.remote.state.ResourceState
import com.bm.hdsbf.data.repository.google.GoogleRepository
import com.bm.hdsbf.data.repository.google.GoogleSheetScraper
import com.bm.hdsbf.ui.setting.ShowSetting
import com.bm.hdsbf.utils.CalendarUtil.getMonthNowAndAfter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.Calendar
import javax.inject.Inject

class ScheduleRepository @Inject constructor(
    private val googleRepository: GoogleRepository,
    private val googleSheetScraper: GoogleSheetScraper,
    private val scheduleDao: ScheduleDao,
    private val preferenceClass: PreferenceClass
) {

    fun getAllData(): Flow<ResourceState<Int>> {
        return flow {
            emit(ResourceState.OnLoading(true))
            try {
                var lastUpdate: Long = 0
                googleRepository.getLastUpdateSchedule().collect { lastUpdate = it }
                if (lastUpdate > preferenceClass.getLastModified()) {
                    val monthAvailable = mutableListOf<String>()
                    googleSheetScraper.getMonthAvailable().collect {
                        monthAvailable.addAll(it)
                    }
                    val monthSaved = scheduleDao.getAllMonth().toMutableList()

                    val monthNotSaved = mutableSetOf<String>()
                    monthAvailable.forEach {
                        if (!monthSaved.remove(it)) monthNotSaved.add(it)
                    }
                    if (monthSaved.isNotEmpty()) scheduleDao.deleteMonths(monthSaved)

                    val monthNowAndAfter = getMonthNowAndAfter()
                    monthNotSaved.addAll(monthNowAndAfter)
                    scheduleDao.deleteMonths(monthNowAndAfter)

                    monthNotSaved.forEachIndexed { index, sheetName ->
                        emit(ResourceState.OnSuccess(((index.toDouble() * 100.0) / monthNotSaved.size.toDouble()).toInt()))
                        googleRepository.getDataSheet(sheetName)
                            .collect { list -> scheduleDao.insertAll(list) }
                    }
                    preferenceClass.setLastModified(Calendar.getInstance().timeInMillis)
                }
                emit(ResourceState.OnSuccess(100))
            } catch (e: Exception) {
                e.printStackTrace()
                emit(ResourceState.OnError(e.localizedMessage?.toString() ?: ""))
            } finally {
                emit(ResourceState.OnLoading(false))
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getDataCount(): Flow<ResourceState<HashMap<String, HashMap<Int, List<String>>>>> {
        return flow {
            try {
                emit(ResourceState.OnLoading(true))
                val allData =
                    if (preferenceClass.getShow().let { it == null || it == ShowSetting.ALL.text() }) scheduleDao.getAllSchedule()
                    else scheduleDao.getAllScheduleByName(preferenceClass.getName()!!)
                val hashMapMonth = hashMapOf<String, HashMap<Int, List<String>>>()
                allData.groupBy { it.month }.forEach { groupMonth ->
                    val month = groupMonth.key
                    val hashMapDate = hashMapOf<Int, List<String>>()
                    groupMonth.value.groupBy { it.date }.forEach { groupDate ->
                        val date = groupDate.key
                        val count = groupDate.value.map { it.type }
                        hashMapDate[date] = count
                    }
                    hashMapMonth[month] = hashMapDate
                }
                emit(ResourceState.OnSuccess(hashMapMonth))
            } catch (e: Exception) {
                e.printStackTrace()
                emit(ResourceState.OnError(e.localizedMessage?.toString() ?: ""))
            } finally {
                emit(ResourceState.OnLoading(false))
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getScheduleByDate(month: String, date: Int): Flow<ResourceState<List<ScheduleVo>>> {
        return flow {
            try {
                emit(ResourceState.OnLoading(true))
                val dataSchedule = scheduleDao.getScheduleByDate(month, date)
                emit(ResourceState.OnSuccess(dataSchedule))
            } catch (e: Exception) {
                e.printStackTrace()
                emit(ResourceState.OnError(e.localizedMessage?.toString() ?: ""))
            } finally {
                emit(ResourceState.OnLoading(false))
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getName(month: String): Flow<ResourceState<List<String>>> {
        return flow {
            try {
                emit(ResourceState.OnLoading(true))
                val dataName = scheduleDao.getName(month)
                emit(ResourceState.OnSuccess(dataName))
            } catch (e: Exception) {
                emit(ResourceState.OnError(e.localizedMessage?.toString() ?: ""))
            } finally {
                emit(ResourceState.OnLoading(false))
            }
        }.flowOn(Dispatchers.IO)
    }
}