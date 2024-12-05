package com.yikwing.extension

import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren

/**
 * <pre>
 *     author: yikwing
 *
 *        _ _              _
 *       (_) |            (_)
 *  _   _ _| | ____      ___ _ __   __ _
 * | | | | | |/ /\ \ /\ / / | '_ \ / _` |
 * | |_| | |   <  \ V  V /| | | | | (_| |
 *  \__, |_|_|\_\  \_/\_/ |_|_| |_|\__, |
 *   __/ |                          __/ |
 *  |___/                          |___/
 *
 *     email : 49999@live.com
 *     time  : 2024-12-05 22:17
 *     desc  : CoroutineUtils
 * </pre>
 */
object CoroutineScopeFactory {
    private const val TAG = "CoroutineScopeFactory"

    /**
     * 创建一个 CoroutineScope
     * @param dispatcher 调度器，默认为 Dispatchers.Default
     * @param errorHandler 全局异常处理器，默认为打印日志的异常处理器
     */
    fun createScope(
        dispatcher: CoroutineDispatcher = Dispatchers.Default,
        errorHandler: CoroutineExceptionHandler? =
            CoroutineExceptionHandler { _, throwable ->
                Log.e(TAG, "Unhandled exception: ${throwable.message}")
            },
    ): CoroutineScope {
        val job = SupervisorJob()
        val context =
            if (errorHandler != null) {
                dispatcher + job + errorHandler
            } else {
                dispatcher + job
            }
        return CoroutineScope(context)
    }

    /**
     * 取消指定的 CoroutineScope
     * @param scope 需要取消的协程作用域
     */
    fun cancelScope(scope: CoroutineScope) {
        if (scope.coroutineContext[Job]?.isActive == true) {
            scope.coroutineContext.cancelChildren()
            scope.coroutineContext[Job]?.cancel()
            Log.d(TAG, "CoroutineScope cancelled successfully.")
        } else {
            Log.e(TAG, "CoroutineScope is already cancelled.")
        }
    }

    /**
     * 快速创建一个 MainScope
     */
    fun createMainScope(errorHandler: CoroutineExceptionHandler? = null): CoroutineScope = createScope(Dispatchers.Main, errorHandler)

    /**
     * 快速创建一个 IOScope
     */
    fun createIOScope(errorHandler: CoroutineExceptionHandler? = null): CoroutineScope = createScope(Dispatchers.IO, errorHandler)
}
