package com.bm.hdsbf.ui.shiftswap

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bm.hdsbf.data.local.db.entities.ScheduleVo
import com.bm.hdsbf.data.remote.Resource
import com.bm.hdsbf.data.repository.google.GoogleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShitSwapViewModel @Inject constructor(
    private val googleRepository: GoogleRepository
): ViewModel() {
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _updateResult = MutableLiveData<Boolean>()
    val updateResult: LiveData<Boolean> get() = _updateResult

    fun swapSchedule(origin: ScheduleVo, destination: ScheduleVo) {
        viewModelScope.launch {
            googleRepository.swapSchedule(origin, destination).collect {
                when (it) {
                    is Resource.OnLoading -> _loading.postValue(it.isLoading)
                    is Resource.OnError -> _error.postValue(it.message)
                    is Resource.OnSuccess -> _updateResult.postValue(it.data)
                }
            }
        }
    }
}