package com.bm.hdsbf.data.repository.google

import android.util.Log
import com.bm.hdsbf.BuildConfig
import com.bm.hdsbf.data.local.db.entities.ScheduleVo
import com.bm.hdsbf.data.local.sp.PreferenceClass
import com.bm.hdsbf.data.remote.Resource
import com.bm.hdsbf.data.remote.config.RemoteConfig
import com.bm.hdsbf.data.remote.service.GoogleService
import com.bm.hdsbf.utils.CalendarUtil.getMonthNowAndAfter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GoogleRepository @Inject constructor(
    private val remoteConfig: RemoteConfig,
    private val preferenceClass: PreferenceClass,
    private val googleService: GoogleService
) {

    private val monthNowAndAfter by lazy { getMonthNowAndAfter() }

    companion object {
        private const val TAG = "GoogleRepository"
    }

    fun getLastUpdateSchedule(): Flow<Long> {
        return flow {
            try {
                val service = googleService.getDriveService()
                val fileMetadata = service.files().get(remoteConfig.getSpreadsheetId())
                    .setFields("modifiedTime")
                    .execute()
                val time = fileMetadata.modifiedTime.value
                emit(time)
            } catch (e: Exception) {
                Log.e(TAG, "getLastUpdateSchedule error: ${e.message}", e)
                emit(0)
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getDataSheet(sheetName: String): Flow<List<ScheduleVo>> {
        val list = mutableListOf<ScheduleVo>()
        val spreadsheetId = remoteConfig.getSpreadsheetId()

        val ranges = listOf(
            "$sheetName!${remoteConfig.getRangeSchedule()}",
            "$sheetName!${remoteConfig.getRangeOff()}",
            "$sheetName!${remoteConfig.getRangeTime()}"
        )

        return flow {
            try {
                val service = googleService.getSheetsService()
                val response = service.spreadsheets().values()
                    .batchGet(spreadsheetId)
                    .setRanges(ranges)
                    .execute()

                val valueRanges = response.valueRanges

                valueRanges.getOrNull(0)?.getValues()?.let { values ->
                    val mutableValues = values.toMutableList()
                    if (mutableValues.isNotEmpty()) mutableValues.removeAt(0)

                    mutableValues.forEach { row ->
                        var name = ""
                        row.forEachIndexed { i, value ->
                            val strValue = value.toString()
                            if (i == 1) name = strValue
                            if (i > 1 && strValue.contains("HD")) {
                                list.add(ScheduleVo(
                                    id = "$strValue$name${i - 1}$sheetName".hashCode(),
                                    month = sheetName,
                                    date = i - 1,
                                    name = name,
                                    type = strValue
                                ))
                            }
                        }
                    }
                }

                valueRanges.getOrNull(1)?.getValues()?.let { values ->
                    val mutableValues = values.toMutableList()
                    if (mutableValues.isNotEmpty()) mutableValues.removeAt(0)

                    mutableValues.forEach { row ->
                        var name = ""
                        row.forEachIndexed { i, value ->
                            val strValue = value.toString()
                            if (i == 1) name = strValue
                            if (i > 1 && strValue.length > 3) {
                                list.add(ScheduleVo(
                                    id = "OFF-$strValue$name${i - 1}$sheetName".hashCode(),
                                    month = sheetName,
                                    date = i - 1,
                                    name = name,
                                    type = "OFF-$strValue"
                                ))
                            }
                        }
                    }
                }

                if (sheetName == monthNowAndAfter[0]) {
                    valueRanges.getOrNull(2)?.getValues()?.let { values ->
                        val hash = hashMapOf<String, String>()
                        values.forEach { row ->
                            if (row.size >= 2) {
                                hash[row.first().toString()] = row.last().toString()
                            }
                        }
                        preferenceClass.setTimeSchedule(hash)
                    }
                }

                emit(list)
            } catch (e: Exception) {
                Log.e(TAG, "getDataSheet error for $sheetName: ${e.message}", e)
                emit(list)
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getLastUpdateApp(): Flow<Resource<Boolean?>> {
        return flow {
            emit(Resource.OnLoading(true))
            try {
                val driveService = googleService.getDriveService()
                val appId = remoteConfig.getAppId()
                val fileMetadata = driveService.files().get(appId)
                    .setFields("name")
                    .execute()

                val name = fileMetadata.name
                if (name != null) {
                    val nameSplit = name.split("-")
                    val versionNumber = nameSplit.last().removeSuffix(".apk").toIntOrNull() ?: 0

                    if (versionNumber > BuildConfig.VERSION_CODE) {
                        if (nameSplit.getOrNull(1)?.contains("f", ignoreCase = true) == true) {
                            emit(Resource.OnSuccess(true))
                        } else {
                            emit(Resource.OnSuccess(false))
                        }
                    } else {
                        emit(Resource.OnSuccess(null))
                    }
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
}