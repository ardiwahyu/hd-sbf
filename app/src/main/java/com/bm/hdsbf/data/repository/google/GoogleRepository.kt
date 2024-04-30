package com.bm.hdsbf.data.repository.google

import com.bm.hdsbf.BuildConfig
import com.bm.hdsbf.data.local.db.entities.ScheduleVo
import com.bm.hdsbf.data.remote.service.GoogleDriveService
import com.bm.hdsbf.data.remote.service.GoogleSheetService
import com.bm.hdsbf.data.remote.state.ResourceState
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
    private val googleSheetService: GoogleSheetService
) {

    fun getLastUpdateSchedule(): Flow<Long> {
        return flow {
            val response = googleDriveService.getLastModifiedSchedule()
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
                rangesSchedule = "$sheetName!A2:AK16",
                rangesOff = "$sheetName!A25:AK39"
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
                emit(list)
            } else emit(list)
        }.flowOn(Dispatchers.IO)
    }

    fun getLastUpdateApp(): Flow<ResourceState<Boolean?>> {
        return flow {
            emit(ResourceState.OnLoading(true))
            try {
                val response = googleDriveService.getLastModifiedApp()
                if (response.isSuccessful) {
                    val jsonResponse = response.body()
                    val name = jsonResponse?.get("name")?.asString!!
                    val nameSplit = name.split("-")
                    val versionNumber = nameSplit.last().removeSuffix(".apk").toInt()
                    if (versionNumber > BuildConfig.VERSION_CODE) {
                        if (nameSplit[1].contains("f", ignoreCase = true)) {
                            emit(ResourceState.OnSuccess(true))
                        } else emit(ResourceState.OnSuccess(false))
                    } else {
                        emit(ResourceState.OnSuccess(null))
                    }
                } else {
                    emit(ResourceState.OnSuccess(null))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emit(ResourceState.OnSuccess(null))
            } finally {
                emit(ResourceState.OnLoading(false))
            }
        }.flowOn(Dispatchers.IO)
    }
}