package com.bm.hdsbf.data.repository.schedule

import com.bm.hdsbf.data.local.db.dao.ScheduleDao
import com.bm.hdsbf.data.local.db.entities.ScheduleVo
import com.bm.hdsbf.data.local.sp.PreferenceClass
import com.bm.hdsbf.data.remote.Resource
import com.bm.hdsbf.data.remote.config.RemoteConfig
import com.bm.hdsbf.data.remote.service.GoogleService
import com.bm.hdsbf.data.repository.google.GoogleRepository
import com.bm.hdsbf.ui.setting.ShowSetting
import com.bm.hdsbf.utils.CalendarUtil.getMonthNowAndAfter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class ScheduleRepository @Inject constructor(
    private val googleService: GoogleService,
    private val googleRepository: GoogleRepository,
    private val scheduleDao: ScheduleDao,
    private val preferenceClass: PreferenceClass,
    private val remoteConfig: RemoteConfig
) {

    private val monthNowAndAfter by lazy { getMonthNowAndAfter() }
    private val df = SimpleDateFormat("MMMM yyyy", Locale("ID"))

    private fun getMonthAvailable(): Flow<List<String>> {
        val list = mutableListOf<String>()
        val spreadsheetId = remoteConfig.getSpreadsheetId()
        return flow {
            try {
                val service = googleService.getSheetsService()
                val response = service.spreadsheets().get(spreadsheetId)
                    .setFields("sheets.properties.title")
                    .execute()
                response.sheets?.forEach { sheet ->
                    val tabName = sheet.properties.title
                    try {
                        df.parse(tabName)
                        list.add(tabName)
                    } catch (e: Exception) { }
                }

                emit(list)
            } catch (e: Exception) {
                e.printStackTrace()
                emit(list)
            }
        }.catch {
            it.printStackTrace()
            emit(list)
        }.flowOn(Dispatchers.IO)
    }

    fun getAllData(): Flow<Resource<Int>> {
        return flow {
            emit(Resource.OnLoading(true))
            try {
                val monthSaved = scheduleDao.getAllMonth().toMutableList()
                if (monthSaved.isEmpty()) { // cek jika baru update, dan database baru
                    preferenceClass.setLastModified(0)
                }

                var lastUpdate: Long = 0
                googleRepository.getLastUpdateSchedule().collect { lastUpdate = it }
                if (lastUpdate > preferenceClass.getLastModified()) {
                    val monthAvailable = mutableListOf<String>()
                    getMonthAvailable().collect {
                        monthAvailable.addAll(it)
                    }

                    val monthNotSaved = mutableSetOf<String>()
                    monthAvailable.forEach {
                        if (!monthSaved.remove(it)) monthNotSaved.add(it)
                    }
                    if (monthSaved.isNotEmpty()) scheduleDao.deleteMonths(monthSaved)

                    monthNotSaved.addAll(monthNowAndAfter)
                    scheduleDao.deleteMonths(monthNowAndAfter)

                    monthNotSaved.forEachIndexed { index, sheetName ->
                        emit(Resource.OnSuccess((((index + 1).toDouble() * 100.0) / monthNotSaved.size.toDouble()).toInt()))
                        googleRepository.getDataSheet(sheetName)
                            .collect { list -> scheduleDao.insertAll(list) }
                    }
                    preferenceClass.setLastModified(Calendar.getInstance().timeInMillis)
                }
                emit(Resource.OnSuccess(100))
            } catch (e: Exception) {
                e.printStackTrace()
                emit(Resource.OnError(e.localizedMessage ?: ""))
            } finally {
                emit(Resource.OnLoading(false))
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getDataCount(): Flow<Resource<HashMap<String, HashMap<Int, List<String>>>>> {
        return flow {
            try {
                emit(Resource.OnLoading(true))
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
                emit(Resource.OnSuccess(hashMapMonth))
            } catch (e: Exception) {
                e.printStackTrace()
                emit(Resource.OnError(e.localizedMessage ?: ""))
            } finally {
                emit(Resource.OnLoading(false))
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getScheduleByDate(month: String, date: Int): Flow<Resource<List<ScheduleVo>>> {
        return flow {
            try {
                emit(Resource.OnLoading(true))
                val dataSchedule = scheduleDao.getScheduleByDate(month, date)
                emit(Resource.OnSuccess(dataSchedule))
            } catch (e: Exception) {
                e.printStackTrace()
                emit(Resource.OnError(e.localizedMessage ?: ""))
            } finally {
                emit(Resource.OnLoading(false))
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getName(month: String): Flow<Resource<List<String>>> {
        return flow {
            try {
                emit(Resource.OnLoading(true))
                val dataName = scheduleDao.getName(month)
                emit(Resource.OnSuccess(dataName))
            } catch (e: Exception) {
                emit(Resource.OnError(e.localizedMessage ?: ""))
            } finally {
                emit(Resource.OnLoading(false))
            }
        }.flowOn(Dispatchers.IO)
    }
}