package com.yikwing.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * flow 扩展
 *
 * 判断errorCode的状态
 * */

inline fun <T> transformApi(crossinline block: suspend () -> BaseHttpResult<T>) =
    flow {
        emit(RequestState.Loading)
        runCatching {
            block()
        }.fold(
            onSuccess = { result ->
                if (result.errorCode != 0) {
                    emit(RequestState.Error(ApiException(result.errorCode, result.errorMsg)))
                } else {
                    emit(RequestState.Success(result.data))
                }
            },
            onFailure = { e ->
                emit(RequestState.Error(ApiException.createDefault(e.message, e)))
            },
        )
    }.flowOn(Dispatchers.IO).catch { exception ->
        Log.e("transformApi", "Error: ${exception.message}", exception)
        emit(RequestState.Error(ApiException.createDefault(exception.message, exception)))
    }
