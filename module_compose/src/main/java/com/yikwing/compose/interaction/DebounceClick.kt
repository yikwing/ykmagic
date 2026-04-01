package com.yikwing.compose.interaction

import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.yikwing.compose.util.Ref

@Composable
fun rememberDebounceClick(
    debounceMs: Long = 500L,
    onClick: () -> Unit,
): () -> Unit {
    val currentOnClick by rememberUpdatedState(onClick)
    val lastClickTime = remember { Ref(0L) }

    return remember(debounceMs) {
        {
            val now = SystemClock.elapsedRealtime()
            if (now - lastClickTime.value > debounceMs) {
                lastClickTime.value = now
                currentOnClick()
            }
        }
    }
}
