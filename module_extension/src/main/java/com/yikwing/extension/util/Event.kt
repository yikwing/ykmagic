package com.yikwing.extension.util

/**
 * 一次性事件包装类，用于处理 LiveData/StateFlow 中只应消费一次的事件
 * 典型场景：Toast、Snackbar、导航等一次性 UI 事件
 *
 * @param T 事件内容类型
 * @param content 事件内容
 */
class Event<out T>(
    private val content: T,
) {
    private var hasBeenHandled = false

    /**
     * 获取事件内容，仅首次调用返回内容，后续调用返回 null
     */
    fun getContentIfNotHandled(): T? =
        if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }

    /**
     * 获取事件内容，不影响已处理状态（可重复获取）
     */
    fun peekContent(): T = content
}
