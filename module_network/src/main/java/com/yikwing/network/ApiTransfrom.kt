package com.yikwing.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.FileDescriptor
import kotlin.coroutines.cancellation.CancellationException

/**
 * flow 扩展
 *
 * 判断errorCode的状态
 * */

inline fun <T> transformApi(
    description: String = "",
    crossinline block: suspend () -> BaseHttpResult<T>,
) = flow {
    emit(RequestState.Loading)
    val result = block()
    if (result.errorCode != 0) {
        emit(RequestState.Error(ApiException(result.errorCode, result.errorMsg)))
    } else {
        emit(RequestState.Success(result.data))
    }
}.flowOn(Dispatchers.IO).catch { exception ->
    // 检查是否是取消异常，如果是，则重新抛出
    if (exception is CancellationException) {
        throw exception
    }
    // 所有异常都在这里被捕捉
    Log.e("transformApi", "$description Error: ${exception.message}", exception)
    emit(RequestState.Error(ApiException.createDefault(exception.message, exception)))
}
