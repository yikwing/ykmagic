package com.yikwing.compose.lifecycle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * 监听宿主 LifecycleOwner（通常是宿主 Activity）的前后台切换。
 *
 * **注意**：此处使用 [LocalLifecycleOwner]，绑定的是 Composable 所在的最近
 * LifecycleOwner（Activity 或 Fragment），**不等同于** ProcessLifecycleOwner。
 * 在 Fragment 嵌套场景下，Fragment 销毁时也会触发 [onBackground]，
 * 并不代表 App 真正退到后台。
 *
 * 若需监听整个 App 的前后台切换，应在 Activity 层使用
 * `ProcessLifecycleOwner.get().lifecycle` 注册观察者。
 *
 * onStart = 宿主回到前台，onStop = 宿主退到后台。
 *
 * 使用示例：
 * ```
 * @Composable
 * fun MyScreen() {
 *     AppLifecycleObserver(
 *         onForeground = { viewModel.onForeground() },
 *         onBackground = { viewModel.onBackground() },
 *     )
 * }
 * ```
 */
@Composable
fun AppLifecycleObserver(
    onForeground: () -> Unit = {},
    onBackground: () -> Unit = {},
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val currentOnForeground by rememberUpdatedState(onForeground)
    val currentOnBackground by rememberUpdatedState(onBackground)

    DisposableEffect(lifecycle) {
        val observer =
            object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) = currentOnForeground()

                override fun onStop(owner: LifecycleOwner) = currentOnBackground()
            }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
}
