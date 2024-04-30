package com.bm.hdsbf.ui.setting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bm.hdsbf.data.remote.state.ResourceState
import com.bm.hdsbf.data.repository.schedule.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository
): ViewModel() {
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _listName = MutableLiveData<List<String>>()
    val listName: LiveData<List<String>> get() = _listName

    fun getName(month: String) {
        viewModelScope.launch {
            scheduleRepository.getName(month).collect {
                when (it) {
                    is ResourceState.OnLoading -> _loading.postValue(it.isLoading)
                    is ResourceState.OnError -> _error.postValue(it.message)
                    is ResourceState.OnSuccess -> _listName.postValue(it.data)
                }
            }
        }
    }
}