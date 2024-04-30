package com.bm.hdsbf.ui.schedule

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bm.hdsbf.data.local.db.entities.ScheduleVo
import com.bm.hdsbf.data.remote.state.ResourceState
import com.bm.hdsbf.data.repository.schedule.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository
): ViewModel() {
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _dataCount = MutableLiveData<HashMap<String, HashMap<Int, List<String>>>>()
    val dataCount: LiveData<HashMap<String, HashMap<Int, List<String>>>> get() = _dataCount

    fun getDataCount() {
        viewModelScope.launch {
            scheduleRepository.getDataCount().collect {
                when (it) {
                    is ResourceState.OnLoading -> _loading.postValue(it.isLoading)
                    is ResourceState.OnError -> _error.postValue(it.message)
                    is ResourceState.OnSuccess -> _dataCount.postValue(it.data)
                }
            }
        }
    }

    private val _listSchedule = MutableLiveData<List<ScheduleVo>>()
    val listSchedule: LiveData<List<ScheduleVo>> get() = _listSchedule

    fun getScheduleByDate(month: String, date: Int) {
        viewModelScope.launch {
            scheduleRepository.getScheduleByDate(month, date).collect {
                when (it) {
                    is ResourceState.OnLoading -> _loading.postValue(it.isLoading)
                    is ResourceState.OnError -> _error.postValue(it.message)
                    is ResourceState.OnSuccess -> _listSchedule.postValue(it.data)
                }
            }
        }
    }
}