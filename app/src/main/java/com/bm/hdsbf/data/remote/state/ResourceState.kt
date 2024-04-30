package com.bm.hdsbf.data.remote.state

sealed class ResourceState<out R> {
    data class OnLoading(val isLoading: Boolean) : ResourceState<Nothing>()
    data class OnSuccess<R>(val data: R) : ResourceState<R>()
    data class OnError(val message: String) : ResourceState<Nothing>()
}