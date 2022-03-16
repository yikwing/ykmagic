package com.yk.yknetwork

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

/**
 * flow 扩展
 *
 * 判断errorCode的状态
 * */
fun <T> transformApi(block: BaseHttpResult<T>) = flow {
    emit(block)
}.flowOn(Dispatchers.IO)
    .onStart {
        Log.d("onStart", "onStart表示最开始调用方法之前执行的操作，这里是展示一个 loading ui；")
    }
    .transform { value ->
        if (value.errorCode == 0) {
            emit(value.data)
        } else {
            throw ApiException(value.errorCode, value.errorMsg)
        }
    }
    .onCompletion {
        Log.d("onCompletion", "onCompletion表示所有执行完成，不管有没有异常都会执行这个回调。")
    }