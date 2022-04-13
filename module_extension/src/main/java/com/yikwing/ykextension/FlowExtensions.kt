package com.yikwing.ykextension

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


/**
 * 防抖
 * */
fun <T> Flow<T>.throttleFirst(thresholdMillis: Long): Flow<T> = flow {

    var lastTime = 0L

    collect { upstream ->
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastTime > thresholdMillis) {
            lastTime = currentTime
            emit(upstream)
        }
    }
}
