package com.bm.hdsbf.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bm.hdsbf.data.remote.state.ResourceState
import com.bm.hdsbf.data.repository.google.GoogleRepository
import com.bm.hdsbf.data.repository.schedule.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val googleRepository: GoogleRepository
): ViewModel() {
    private val _loadingSchedule = MutableLiveData<Boolean>()
    val loadingSchedule: LiveData<Boolean> get() = _loadingSchedule

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _progress = MutableLiveData<Int>()
    val progress: LiveData<Int> get() = _progress

    fun getAllData() {
        viewModelScope.launch {
            scheduleRepository.getAllData().collect {
                when (it) {
                    is ResourceState.OnLoading -> _loadingSchedule.postValue(it.isLoading)
                    is ResourceState.OnError -> _error.postValue(it.message)
                    is ResourceState.OnSuccess -> _progress.postValue(it.data)
                }
            }
        }
    }

    private val _loadingUpdate = MutableLiveData<Boolean>()
    val loadingUpdate: LiveData<Boolean> get() = _loadingUpdate

    private val _forceUpdate = MutableLiveData<Boolean?>()
    val forceUpdate: LiveData<Boolean?> get() = _forceUpdate

    fun getLastVersionApp() {
        viewModelScope.launch {
            googleRepository.getLastUpdateApp().collect {
                when (it) {
                    is ResourceState.OnLoading -> _loadingUpdate.postValue(it.isLoading)
                    is ResourceState.OnError -> _error.postValue(it.message)
                    is ResourceState.OnSuccess -> _forceUpdate.postValue(it.data)
                }
            }
        }
    }
}