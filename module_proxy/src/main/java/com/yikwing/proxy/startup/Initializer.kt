package com.yikwing.proxy.startup

import android.content.Context

interface Initializer<T> {
    fun create(context: Context): T

    fun dependencies(): Set<Class<out Initializer<*>>>
}
