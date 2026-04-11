package com.yikwing.extension.coroutines

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

/**
 * Collect a Flow in a lifecycle-aware manner, automatically cancelling
 * when the lifecycle drops below [lifecycleState].
 */
fun <T> Flow<T>.collectInLifecycle(
    lifecycleOwner: LifecycleOwner,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    action: suspend (T) -> Unit,
) {
    lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.repeatOnLifecycle(lifecycleState) {
            collect { action(it) }
        }
    }
}

/**
 * 防抖 (throttleFirst)
 * 在指定时间窗口内，只发射第一个元素，忽略后续元素
 * @param thresholdMillis 时间窗口（毫秒），必须为正数
 * @throws IllegalArgumentException 如果 thresholdMillis 小于等于 0
 */
fun <T> Flow<T>.throttleFirst(thresholdMillis: Long): Flow<T> {
    require(thresholdMillis > 0) { "thresholdMillis must be positive, but was $thresholdMillis" }

    return flow {
        var lastTime = 0L

        collect { upstream ->
            val currentTime = System.currentTimeMillis()

            if (currentTime - lastTime >= thresholdMillis) {
                lastTime = currentTime
                emit(upstream)
            }
        }
    }
}
