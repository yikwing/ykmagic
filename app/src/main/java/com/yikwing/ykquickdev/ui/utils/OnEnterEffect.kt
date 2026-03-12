package com.yikwing.ykquickdev.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

/**
 * 进入时执行一次，不需要写 onDispose
 */
@Composable
fun OnEnterEffect(
    key: Any? = Unit,
    effect: () -> Unit,
) {
    DisposableEffect(key) {
        effect()
        onDispose { }
    }
}

@Composable
fun OnEnterEffect(
    key1: Any?,
    key2: Any?,
    effect: () -> Unit,
) {
    DisposableEffect(key1, key2) {
        effect()
        onDispose { }
    }
}
