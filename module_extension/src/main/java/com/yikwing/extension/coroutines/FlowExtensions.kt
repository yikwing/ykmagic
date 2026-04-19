package com.yikwing.extension.coroutines

import android.os.SystemClock
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

/**
 * Collect a Flow in a lifecycle-aware manner, automatically cancelling
 * when the lifecycle drops below [lifecycleState].
 *
 * @return the [Job] running the collection, so callers can cancel it explicitly if needed.
 */
fun <T> Flow<T>.collectInLifecycle(
    lifecycleOwner: LifecycleOwner,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    action: suspend (T) -> Unit,
): Job =
    lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.repeatOnLifecycle(lifecycleState) {
            collect { action(it) }
        }
    }

/**
 * 防抖 (throttleFirst)
 * 在指定时间窗口内，只发射第一个元素，忽略后续元素
 *
 * 使用 [SystemClock.elapsedRealtime] 单调时钟，避免系统时间回拨导致节流异常。
 *
 * @param thresholdMillis 时间窗口（毫秒），必须为正数
 * @throws IllegalArgumentException 如果 thresholdMillis 小于等于 0
 */
fun <T> Flow<T>.throttleFirst(thresholdMillis: Long): Flow<T> {
    require(thresholdMillis > 0) { "thresholdMillis must be positive, but was $thresholdMillis" }

    return flow {
        var lastTime = 0L

        collect { upstream ->
            val currentTime = SystemClock.elapsedRealtime()

            if (currentTime - lastTime >= thresholdMillis) {
                lastTime = currentTime
                emit(upstream)
            }
        }
    }
}
