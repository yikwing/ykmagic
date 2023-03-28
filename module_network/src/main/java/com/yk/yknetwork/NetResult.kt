package com.yk.yknetwork

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.StateFlow

typealias StatefulLiveData<T> = LiveData<RequestState<T>>
typealias StatefulFlow<T> = StateFlow<RequestState<T>>

sealed interface RequestState<out T> {
    object Loading : RequestState<Nothing>
    class Success<out T>(val value: T) : RequestState<T>
    class Error(val throwable: Throwable) : RequestState<Nothing>
}

inline fun <reified T> RequestState<T>.doSuccess(success: (T) -> Unit) {
    if (this is RequestState.Success) {
        success(value)
    }
}

inline fun <reified T> RequestState<T>.doError(failure: (Throwable?) -> Unit) {
    if (this is RequestState.Error) {
        failure(throwable)
    }
}

class ResultBuilder<T> {
    var onLoading: () -> Unit = {}
    var onSuccess: (data: T) -> Unit = { }
    var onError: (e: Throwable) -> Unit = { }
}

@MainThread
inline fun <T> StatefulLiveData<T>.observeState(
    owner: LifecycleOwner,
    init: ResultBuilder<T>.() -> Unit,
) {
    val result = ResultBuilder<T>().apply(init)

    observe(owner) { state ->
        when (state) {
            is RequestState.Loading -> result.onLoading.invoke()
            is RequestState.Success -> result.onSuccess(state.value)
            is RequestState.Error -> result.onError(state.throwable)
        }
    }
}

@MainThread
suspend inline fun <T> StatefulFlow<T>.collectState(
    init: ResultBuilder<T>.() -> Unit,
) {
    val result = ResultBuilder<T>().apply(init)

    collect { state ->
        when (state) {
            is RequestState.Loading -> result.onLoading.invoke()
            is RequestState.Success -> result.onSuccess(state.value)
            is RequestState.Error -> result.onError(state.throwable)
        }
    }
}
