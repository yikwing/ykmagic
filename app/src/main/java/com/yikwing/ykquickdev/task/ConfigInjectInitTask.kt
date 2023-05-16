package com.yikwing.ykquickdev.task

import android.app.Application
import android.content.Context
import com.yk.ykproxy.startup.Initializer
import com.yikwing.ykquickdev.BuildConfig
import com.yk.ykconfig.YkQuickManager

class ConfigInjectInitTask : Initializer<Unit> {
    override fun create(context: Application) {
        YkQuickManager.setUp(
            BuildConfig.YK_CONFIG,
        )
    }

    override fun dependencies(): Set<Class<out Initializer<*>>> = setOf(
        LoggerInitTask::class.java,
    )
}
