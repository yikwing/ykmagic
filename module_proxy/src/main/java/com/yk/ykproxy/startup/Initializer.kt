package com.yk.ykproxy.startup

import android.app.Application

interface Initializer<T> {
    fun create(context: Application): T

    fun dependencies(): Set<Class<out Initializer<*>>>
}
