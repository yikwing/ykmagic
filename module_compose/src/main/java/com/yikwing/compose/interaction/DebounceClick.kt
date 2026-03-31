package com.yikwing.compose.interaction

import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class LongRef(
    var initial: Long = 0L,
) : ReadWriteProperty<Any?, Long> {
    override fun getValue(
        thisRef: Any?,
        property: KProperty<*>,
    ): Long = initial

    override fun setValue(
        thisRef: Any?,
        property: KProperty<*>,
        value: Long,
    ) {
        initial = value
    }
}

@Composable
private fun rememberLongRef(initial: Long = 0L): LongRef = remember { LongRef(initial) }

@Composable
fun rememberDebounceClick(
    debounceMs: Long = 500L,
    onClick: () -> Unit,
): () -> Unit {
    val currentOnClick by rememberUpdatedState(onClick)
    var lastClickTime by rememberLongRef()

    return remember {
        {
            val now = SystemClock.elapsedRealtime()
            if (now - lastClickTime > debounceMs) {
                lastClickTime = now
                currentOnClick()
            }
        }
    }
}
