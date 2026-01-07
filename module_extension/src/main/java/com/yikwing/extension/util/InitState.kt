package com.yikwing.extension.util

import androidx.compose.runtime.Immutable

@Immutable
sealed class InitState<out T> {
    data object Uninitialized : InitState<Nothing>()

    data class Value<out T>(
        val data: T,
    ) : InitState<T>()

    val isInitialized: Boolean get() = this is Value

    fun getOrNull(): T? = (this as? Value)?.data

    fun getOrElse(defaultValue: () -> @UnsafeVariance T): T = getOrNull() ?: defaultValue()
}
