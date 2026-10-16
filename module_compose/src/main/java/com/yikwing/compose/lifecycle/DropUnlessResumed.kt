package com.yikwing.compose.lifecycle

import androidx.annotation.CheckResult
import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * 带参数版本的 [androidx.lifecycle.compose.dropUnlessResumed]：仅当 [lifecycleOwner]
 * 处于 RESUMED 时才把参数转交给 [block]，否则丢弃本次调用。
 *
 * 用于带参数的导航回调（如 `(id) -> navigate(Route(id))`），避免页面转场期间重复点击导致多次入栈。
 * 生命周期状态在调用时读取，而不是在组合时读取。
 *
 * @param lifecycleOwner 被观察的 [LifecycleOwner]，默认取 [LocalLifecycleOwner]。
 * @param block 处于 RESUMED 时执行的回调。
 * @return 包装后的回调；非 RESUMED 状态下调用不会执行 [block]。
 */
@CheckResult
@Composable
fun <T> dropUnlessResumed(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    block: (T) -> Unit,
): (T) -> Unit =
    { value ->
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            block(value)
        }
    }
