package com.bm.hdsbf.data.repository.google

import android.util.Log
import com.bm.hdsbf.BuildConfig
import com.bm.hdsbf.data.local.db.dao.ScheduleDao
import com.bm.hdsbf.data.local.db.entities.ScheduleVo
import com.bm.hdsbf.data.local.sp.PreferenceClass
import com.bm.hdsbf.data.remote.Resource
import com.bm.hdsbf.data.remote.config.RemoteConfig
import com.bm.hdsbf.data.remote.service.GoogleService
import com.bm.hdsbf.utils.CalendarUtil.getMonthNowAndAfter
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest
import com.google.api.services.sheets.v4.model.ValueRange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.Calendar
import javax.inject.Inject

class GoogleRepository @Inject constructor(
    private val remoteConfig: RemoteConfig,
    private val preferenceClass: PreferenceClass,
    private val googleService: GoogleService,
    private val scheduleDao: ScheduleDao
) {

    private val monthNowAndAfter by lazy { getMonthNowAndAfter() }

    companion object {
        private const val TAG = "GoogleRepository"
    }

    fun getLastUpdateSchedule(): Flow<Long> = flow {
        try {
            val service = googleService.getDriveService()
            val fileMetadata = service.files().get(remoteConfig.getSpreadsheetId())
                .setFields("modifiedTime")
                .execute()
            emit(fileMetadata.modifiedTime.value)
        } catch (e: Exception) {
            Log.e(TAG, "getLastUpdateSchedule error: ${e.message}", e)
            emit(0L)
        }
    }.flowOn(Dispatchers.IO)

    fun getDataSheet(sheetName: String): Flow<List<ScheduleVo>> = flow {
        val spreadsheetId = remoteConfig.getSpreadsheetId()
        val ranges = listOf(
            "$sheetName!${remoteConfig.getRangeSchedule()}",
            "$sheetName!${remoteConfig.getRangeOff()}",
            "$sheetName!${remoteConfig.getRangeTime()}"
        )

        try {
            val service = googleService.getSheetsService()
            val response = service.spreadsheets().values()
                .batchGet(spreadsheetId)
                .setRanges(ranges)
                .execute()

            val valueRanges = response.valueRanges
            val allSchedules = mutableListOf<ScheduleVo>()

            // Parse Main Schedule
            valueRanges.getOrNull(0)?.getValues()?.let { values ->
                allSchedules.addAll(parseScheduleRows(values, sheetName))
            }

            // Parse Off Schedule
            valueRanges.getOrNull(1)?.getValues()?.let { values ->
                allSchedules.addAll(parseOffRows(values, sheetName))
            }

            // Parse and Save Time Schedule if it's the current month
            if (sheetName == monthNowAndAfter.getOrNull(0)) {
                valueRanges.getOrNull(2)?.getValues()?.let { values ->
                    saveTimeSchedule(values)
                }
            }

            emit(allSchedules)
        } catch (e: Exception) {
            Log.e(TAG, "getDataSheet error for $sheetName: ${e.message}", e)
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    fun swapSchedule(origin: ScheduleVo, destination: ScheduleVo): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.OnLoading(true))
            val service = googleService.getSheetsService()
            val spreadsheetId = remoteConfig.getSpreadsheetId()
            val dataDel1 = ValueRange()
                .setRange("${origin.month}!${origin.column}${origin.row}")
                .setValues(listOf(listOf("")))
            val dataDel2 = ValueRange()
                .setRange("${destination.month}!${destination.column}${destination.row}")
                .setValues(listOf(listOf("")))
            val dataSet1 = ValueRange()
                .setRange("${origin.month}!${origin.column}${destination.row}")
                .setValues(listOf(listOf(origin.type)))
            val dataSet2 = ValueRange()
                .setRange("${destination.month}!${destination.column}${origin.row}")
                .setValues(listOf(listOf(destination.type)))
            val dataList = listOf(dataDel1, dataDel2, dataSet1, dataSet2)
            val body = BatchUpdateValuesRequest().setValueInputOption("USER_ENTERED").setData(dataList)
            service.spreadsheets().values().batchUpdate(spreadsheetId, body).execute()

            scheduleDao.deleteMonths(listOf(origin.month, destination.month))
            getDataSheet(origin.month).collect { scheduleDao.insertAll(it) }
            if (origin.month != destination.month) {
                getDataSheet(destination.month).collect {
                    scheduleDao.insertAll(it)
                }
            }
            preferenceClass.setLastModified(Calendar.getInstance().timeInMillis)
            emit(Resource.OnSuccess(true))
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Resource.OnError(e.localizedMessage ?: ""))
        } finally {
            emit(Resource.OnLoading(false))
        }
    }.flowOn(Dispatchers.IO)

    private fun parseScheduleRows(values: List<List<Any>>, sheetName: String): List<ScheduleVo> {
        if (values.isEmpty()) return emptyList()
        val list = mutableListOf<ScheduleVo>()

        // Ambil angka baris awal dari string range, misal "A2:AK18" -> startRow = 2
        val startRow = getStartRowFromRange(remoteConfig.getRangeSchedule())

        values.forEachIndexed { rowIndex, row ->
            // Tetap skip baris pertama jika itu header
            if (rowIndex == 0) return@forEachIndexed

            val name = row.getOrNull(1)?.toString() ?: ""
            if (name.isBlank()) return@forEachIndexed

            // Baris asli = baris awal range + indeks saat ini
            // Jika startRow = 2 dan rowIndex = 1 (baris setelah header), maka actualRow = 3
            val actualRow = startRow + rowIndex

            row.forEachIndexed { i, value ->
                val strValue = value.toString()
                if (i > 1 && strValue.contains("HD")) {
                    list.add(
                        ScheduleVo(
                            id = "$strValue$name${i - 1}$sheetName".hashCode(),
                            month = sheetName,
                            date = i - 1,
                            name = name,
                            type = strValue,
                            row = actualRow.toString(),
                            column = getColumnName(i)
                        )
                    )
                }
            }
        }
        return list
    }

    private fun parseOffRows(values: List<List<Any>>, sheetName: String): List<ScheduleVo> {
        if (values.isEmpty()) return emptyList()
        val list = mutableListOf<ScheduleVo>()

        val startRow = getStartRowFromRange(remoteConfig.getRangeOff())

        values.forEachIndexed { rowIndex, row ->
            if (rowIndex == 0) return@forEachIndexed

            val name = row.getOrNull(1)?.toString() ?: ""
            if (name.isBlank()) return@forEachIndexed

            val actualRow = startRow + rowIndex

            row.forEachIndexed { i, value ->
                val strValue = value.toString()
                if (i > 1 && strValue.length > 3) {
                    list.add(
                        ScheduleVo(
                            id = "OFF-$strValue$name${i - 1}$sheetName".hashCode(),
                            month = sheetName,
                            date = i - 1,
                            name = name,
                            type = "OFF-$strValue",
                            row = actualRow.toString(),
                            column = getColumnName(i)
                        )
                    )
                }
            }
        }
        return list
    }

    private fun saveTimeSchedule(values: List<List<Any>>) {
        val timeMap = values.associate { row ->
            row.getOrNull(0)?.toString().orEmpty() to row.getOrNull(1)?.toString().orEmpty()
        }.filterKeys { it.isNotBlank() }
        preferenceClass.setTimeSchedule(HashMap(timeMap))
    }

    private fun getStartRowFromRange(range: String): Int {
        // Mengambil angka pertama dari string range menggunakan Regex
        // Contoh: "A2:AK18" -> akan mengambil "2"
        val match = Regex("\\d+").find(range)
        return match?.value?.toInt() ?: 1
    }

    private fun getColumnName(index: Int): String {
        var col = index + 1
        var columnName = ""
        while (col > 0) {
            val rem = (col - 1) % 26
            columnName = ('A' + rem) + columnName
            col = (col - 1) / 26
        }
        return columnName
    }

    fun getLastUpdateApp(): Flow<Resource<Boolean?>> = flow {
        emit(Resource.OnLoading(true))
        try {
            val driveService = googleService.getDriveService()
            val appId = remoteConfig.getAppId()
            val fileMetadata = driveService.files().get(appId)
                .setFields("name")
                .execute()

            val fileName = fileMetadata.name ?: run {
                emit(Resource.OnSuccess(null))
                return@flow
            }

            val nameParts = fileName.split("-")
            val versionNumber = nameParts.lastOrNull()?.removeSuffix(".apk")?.toIntOrNull() ?: 0

            if (versionNumber > BuildConfig.VERSION_CODE) {
                val isForceUpdate = nameParts.getOrNull(1)?.contains("f", ignoreCase = true) == true
                emit(Resource.OnSuccess(isForceUpdate))
            } else {
                emit(Resource.OnSuccess(null))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getLastUpdateApp error: ${e.message}", e)
            emit(Resource.OnSuccess(null))
        } finally {
            emit(Resource.OnLoading(false))
        }
    }.flowOn(Dispatchers.IO)
}
