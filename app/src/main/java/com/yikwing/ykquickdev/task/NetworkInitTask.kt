package com.yikwing.ykquickdev.task

import android.content.Context
import com.yikwing.network.ApiConfig
import com.yikwing.proxy.startup.Initializer

class NetworkInitTask : Initializer<Unit> {
    override fun create(context: Context) {
        ApiConfig.errorCodeChecker = { it != 0 }
    }

    override fun dependencies(): Set<Class<out Initializer<*>>> =
        setOf(
            ConfigInjectInitTask::class.java,
        )
}
