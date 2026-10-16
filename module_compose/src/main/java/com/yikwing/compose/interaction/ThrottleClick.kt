package com.yikwing.compose.interaction

import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.yikwing.compose.util.Ref

/**
 * 立即执行首次点击，仅在距离上次执行至少 [intervalMs] 毫秒后接受下一次点击。
 *
 * [intervalMs] 必须非负，0 表示不限制点击。重组时使用最新的 [onClick]，
 * 修改间隔仍保留上次执行时间；被忽略的点击不会延长等待时间。
 */
@Composable
fun rememberThrottleClick(
    intervalMs: Long = 500L,
    onClick: () -> Unit,
): () -> Unit {
    require(intervalMs >= 0L) { "intervalMs must be non-negative" }

    val currentOnClick by rememberUpdatedState(onClick)
    val lastClickTime = remember { Ref<Long?>(null) }

    return remember(intervalMs) {
        {
            val now = SystemClock.elapsedRealtime()
            val previous = lastClickTime.value
            if (previous == null || now - previous >= intervalMs) {
                lastClickTime.value = now
                currentOnClick()
            }
        }
    }
}
