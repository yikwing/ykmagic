package com.yikwing.ykquickdev.task

import android.content.Context
import com.yikwing.proxy.startup.Initializer
import com.yikwing.ykquickdev.BuildConfig
import com.yikwing.config.YkQuickManager

class ConfigInjectInitTask : Initializer<Unit> {
    override fun create(context: Context) {
        YkQuickManager.setUp(
            BuildConfig.YK_CONFIG,
        )
    }

    override fun dependencies(): Set<Class<out Initializer<*>>> = setOf(
        LoggerInitTask::class.java,
    )
}
