package com.yikwing.extension.view

import android.view.View
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * @Author yikwing
 * @Date 13/4/2022-13:33
 * @Description: View 功能扩展
 */

/**
 * 点击事件
 * */
fun View.clickFlow(): Flow<View> = callbackFlow {
    setOnClickListener { view ->
        trySend(view)
    }

    awaitClose { setOnClickListener(null) }
}
