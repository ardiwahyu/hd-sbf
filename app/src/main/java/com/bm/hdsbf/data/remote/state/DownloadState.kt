package com.bm.hdsbf.data.remote.state

sealed class DownloadState {
    data class Downloading(val progress: Int) : DownloadState()
    data class Finished(val fileName: String): DownloadState()
    data class Failed(val error: Throwable? = null) : DownloadState()
}