package com.bm.hdsbf.ui.update

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bm.hdsbf.data.remote.state.DownloadState
import com.bm.hdsbf.data.repository.updateApp.UpdateAppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val updateAppRepository: UpdateAppRepository
): ViewModel() {
    private val _downloading = MutableLiveData<Int>()
    val downloading: LiveData<Int> get() = _downloading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _downloadFinish = MutableLiveData<String>()
    val downloadFinish: LiveData<String> get() = _downloadFinish

    fun download() {
        viewModelScope.launch {
            updateAppRepository.download().collect {
                when (it) {
                    is DownloadState.Downloading -> _downloading.postValue(it.progress)
                    is DownloadState.Failed -> _error.postValue("err: ${it.error?.localizedMessage}")
                    is DownloadState.Finished -> _downloadFinish.postValue(it.fileName)
                }
            }
        }
    }
}