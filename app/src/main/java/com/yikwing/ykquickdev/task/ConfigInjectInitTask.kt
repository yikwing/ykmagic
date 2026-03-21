package com.yikwing.ykquickdev.task

import android.content.Context
import com.yikwing.config.YkConfigManager
import com.yikwing.proxy.startup.Initializer
import com.yikwing.ykquickdev.BuildConfig

class ConfigInjectInitTask : Initializer<Unit> {
    override fun create(context: Context) {
        YkConfigManager.setUp(BuildConfig.YK_CONFIG)
    }

    override fun dependencies(): Set<Class<out Initializer<*>>> = setOf()
}
