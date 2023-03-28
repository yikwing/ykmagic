package com.yikwing.ykquickdev.task

import android.content.Context
import com.example.studydemo.startup.Initializer
import com.yikwing.ykquickdev.BuildConfig
import com.yk.ykconfig.YkQuickManager

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
