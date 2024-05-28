package com.bm.hdsbf.data.repository.google

import com.bm.hdsbf.BuildConfig
import com.bm.hdsbf.data.local.db.entities.ScheduleVo
import com.bm.hdsbf.data.local.sp.PreferenceClass
import com.bm.hdsbf.data.remote.Resource
import com.bm.hdsbf.data.remote.config.RemoteConfig
import com.bm.hdsbf.data.remote.service.GoogleDriveService
import com.bm.hdsbf.data.remote.service.GoogleSheetService
import com.bm.hdsbf.utils.CalendarUtil.getMonthNowAndAfter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

class GoogleRepository @Inject constructor(
    private val googleDriveService: GoogleDriveService,
    private val googleSheetService: GoogleSheetService,
    private val remoteConfig: RemoteConfig,
    private val preferenceClass: PreferenceClass
) {

    private val monthNowAndAfter by lazy { getMonthNowAndAfter() }

    fun getLastUpdateSchedule(): Flow<Long> {
        return flow {
            val response = googleDriveService.getLastModifiedSchedule(
                id = remoteConfig.getSpreadsheetId()
            )
            if (response.isSuccessful) {
                try {
                    val jsonObject = response.body()
                    val lastModified = jsonObject?.get("modifiedTime")?.asString!!
                    val f = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault())
                    f.timeZone = TimeZone.getTimeZone("UTC")
                    val timeUtc = f.parse(lastModified)!!
                    f.timeZone = TimeZone.getTimeZone("GMT+7")
                    val timeId = f.parse(f.format(timeUtc))
                    emit(timeId?.time ?: 0)
                } catch (e: Exception) {
                    e.printStackTrace()
                    emit(0)
                }
            } else {
                emit(0)
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getDataSheet(sheetName: String): Flow<List<ScheduleVo>> {
        val list = mutableListOf<ScheduleVo>()
        return flow {
            val response = googleSheetService.getData(
                id = remoteConfig.getSpreadsheetId(),
                rangesSchedule = "$sheetName!${remoteConfig.getRangeSchedule()}",
                rangesOff = "$sheetName!${remoteConfig.getRangeOff()}",
                rangesTime = "$sheetName!${remoteConfig.getRangeTime()}"
            )
            if (response.isSuccessful) {
                val sheet = response.body()
                sheet?.valueRanges?.get(0)?.values?.apply {
                    removeAt(0)
                    forEach {
                        var name = ""
                        it.forEachIndexed { i, value ->
                            if (i == 1) name = value
                            if (i > 1) {
                                if (value.contains("HD")) {
                                    list.add(ScheduleVo(month = sheetName, date = i-1, name = name, type = value))
                                }
                            }
                        }
                    }
                }
                sheet?.valueRanges?.get(1)?.values?.apply {
                    removeAt(0)
                    forEach {
                        var name = ""
                        it.forEachIndexed { i, value ->
                            if (i == 1) name = value
                            if (i > 1) {
                                if (value.length > 3) {
                                    list.add(ScheduleVo(month = sheetName, date = i-1, name = name, type = "OFF-$value"))
                                }
                            }
                        }
                    }
                }
                if (sheetName == monthNowAndAfter[0]) {
                    sheet?.valueRanges?.get(2)?.values?.apply {
                        val hash = hashMapOf<String, String>()
                        forEach {
                            hash[it.first()] = it.last()
                        }
                        preferenceClass.setTimeSchedule(hash)
                    }
                }
                emit(list)
            } else emit(list)
        }.flowOn(Dispatchers.IO)
    }

    fun getLastUpdateApp(): Flow<Resource<Boolean?>> {
        return flow {
            emit(Resource.OnLoading(true))
            try {
                val response = googleDriveService.getLastModifiedApp(
                    id = remoteConfig.getAppId()
                )
                if (response.isSuccessful) {
                    val jsonResponse = response.body()
                    val name = jsonResponse?.get("name")?.asString!!
                    val nameSplit = name.split("-")
                    val versionNumber = nameSplit.last().removeSuffix(".apk").toInt()
                    if (versionNumber > BuildConfig.VERSION_CODE) {
                        if (nameSplit[1].contains("f", ignoreCase = true)) {
                            emit(Resource.OnSuccess(true))
                        } else emit(Resource.OnSuccess(false))
                    } else {
                        emit(Resource.OnSuccess(null))
                    }
                } else {
                    emit(Resource.OnSuccess(null))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emit(Resource.OnSuccess(null))
            } finally {
                emit(Resource.OnLoading(false))
            }
        }.flowOn(Dispatchers.IO)
    }
}