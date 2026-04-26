package com.bm.hdsbf.ui.absensi

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bm.hdsbf.data.remote.Resource
import com.bm.hdsbf.data.remote.model.LokasiAbsensi
import com.bm.hdsbf.data.remote.model.UserAbsensi
import com.bm.hdsbf.data.repository.absensi.AbsensiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AbsensiViewModel @Inject constructor(
    private val absensiRepository: AbsensiRepository
): ViewModel() {
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _user = MutableLiveData<UserAbsensi>()
    val user: LiveData<UserAbsensi> get() = _user

    private val _configResult = MutableLiveData<LokasiAbsensi>()
    val configResult: LiveData<LokasiAbsensi> get() = _configResult

    private val _absensiResult = MutableLiveData<String>()
    val absensiResult: LiveData<String> get() = _absensiResult

    fun login(nik: String, pass: String, deviceId: String) {
        viewModelScope.launch {
            absensiRepository.login(nik, pass, deviceId).collect {
                when(it) {
                    is Resource.OnError -> _error.postValue(it.message)
                    is Resource.OnLoading -> _loading.postValue(it.isLoading)
                    is Resource.OnSuccess -> _user.postValue(it.data)
                }
            }
        }
    }

    fun getConfig() {
        viewModelScope.launch {
            absensiRepository.getConfig().collect {
                when(it) {
                    is Resource.OnError -> _error.postValue(it.message)
                    is Resource.OnLoading -> _loading.postValue(it.isLoading)
                    is Resource.OnSuccess -> _configResult.postValue(it.data)
                }
            }
        }
    }

    fun absen(idLoc: Long, lat: Double, long: Double, type: String) {
        viewModelScope.launch {
            absensiRepository.absen(idLoc, lat, long, type).collect {
                when (it) {
                    is Resource.OnError -> _error.postValue(it.message)
                    is Resource.OnLoading -> _loading.postValue(it.isLoading)
                    is Resource.OnSuccess -> _absensiResult.postValue(it.data)
                }
            }
        }
    }
}