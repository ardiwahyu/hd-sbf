package com.bm.hdsbf.data.repository.updateApp

import android.os.Environment
import com.bm.hdsbf.data.remote.service.GoogleDriveService
import com.bm.hdsbf.data.remote.state.DownloadState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import javax.inject.Inject

class UpdateAppRepository @Inject constructor(
    private val googleDriveService: GoogleDriveService
) {

    suspend fun download(): Flow<DownloadState> {
        return flow {
            emit(DownloadState.Downloading(0))
            val destinationFile = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString(),
                "sbfhd-${System.currentTimeMillis()}.apk"
            )
            try {
                googleDriveService.downloadFile().use {
                    it.byteStream().use { inputStream ->
                        destinationFile.outputStream().use { outputStream ->
                            val totalBytes = it.contentLength()
                            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                            var progressBytes = 0L
                            var bytes = inputStream.read(buffer)
                            while (bytes >= 0) {
                                outputStream.write(buffer, 0, bytes)
                                progressBytes += bytes
                                bytes = inputStream.read(buffer)
                                val progress = ((progressBytes*100) / totalBytes).toInt()
                                emit(DownloadState.Downloading(progress))
                            }
                        }
                    }
                }
                emit(DownloadState.Finished(destinationFile.absolutePath))
            } catch (e: Exception) {
                emit(DownloadState.Failed(e))
            }
        }.flowOn(Dispatchers.IO)
    }
}