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

        val result =
            try {
                block()
            } catch (e: Exception) {
                throw ApiException(-1, e.message)
            }

        if (result.errorCode != 0) {
            throw ApiException(result.errorCode, result.errorMsg)
        }

        emit(RequestState.Success(result.data))
    }.flowOn(Dispatchers.IO).catch { exception ->
        Log.e("transformApi", "Error: ${exception.message}", exception)
        emit(RequestState.Error(exception))
    }
