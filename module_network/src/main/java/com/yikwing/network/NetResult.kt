package com.yikwing.network

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.StateFlow

typealias StatefulLiveData<T> = LiveData<RequestState<T>>
typealias StatefulFlow<T> = StateFlow<RequestState<T>>

sealed interface RequestState<out T> {
    data object Loading : RequestState<Nothing>

    data class Success<out T>(
        val value: T,
    ) : RequestState<T>

    data class Error(
        val throwable: ApiException,
    ) : RequestState<Nothing>
}

inline fun <T> RequestState<T>.onSuccess(success: (T) -> Unit): RequestState<T> {
    if (this is RequestState.Success) {
        success(value)
    }
    return this
}

inline fun <T> RequestState<T>.onFailure(failure: (ApiException) -> Unit): RequestState<T> {
    if (this is RequestState.Error) {
        failure(throwable)
    }
    return this
}

class ResultBuilder<T> {
    var onLoading: () -> Unit = {}
    var onSuccess: (data: T) -> Unit = { }
    var onFailure: (e: ApiException) -> Unit = { }

    companion object {
        inline fun <T> build(init: ResultBuilder<T>.() -> Unit) = ResultBuilder<T>().apply(init)
    }
}

@MainThread
inline fun <T> StatefulLiveData<T>.observeState(
    owner: LifecycleOwner,
    init: ResultBuilder<T>.() -> Unit,
) {
    val result = ResultBuilder.build(init)

    observe(owner) { state ->
        when (state) {
            is RequestState.Loading -> result.onLoading.invoke()
            is RequestState.Success -> result.onSuccess(state.value)
            is RequestState.Error -> result.onFailure(state.throwable)
        }
    }
}

@MainThread
suspend inline fun <T> StatefulFlow<T>.collectState(init: ResultBuilder<T>.() -> Unit) {
    val result = ResultBuilder.build(init)

    collect { state ->
        when (state) {
            is RequestState.Loading -> result.onLoading.invoke()
            is RequestState.Success -> result.onSuccess(state.value)
            is RequestState.Error -> result.onFailure(state.throwable)
        }
    }
}
