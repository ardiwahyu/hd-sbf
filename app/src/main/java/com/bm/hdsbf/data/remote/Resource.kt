package com.bm.hdsbf.data.remote

sealed class Resource<out R> {
    data class OnLoading(val isLoading: Boolean) : Resource<Nothing>()
    data class OnSuccess<R>(val data: R) : Resource<R>()
    data class OnError(val message: String) : Resource<Nothing>()
}