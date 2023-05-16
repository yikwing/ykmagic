package com.yikwing.ykquickdev.task

import android.app.Application
import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.yk.ykproxy.startup.Initializer
import com.yikwing.ykquickdev.NetworkConfig
import com.yikwing.ykquickdev.ResultInterceptor
import com.yk.ykconfig.YkConfigManager
import com.yk.yknetwork.RetrofitFactory

class NetworkInitTask : Initializer<Unit> {
    override fun create(context: Application) {
        RetrofitFactory.instance.setup(
            YkConfigManager.getConfig(NetworkConfig::class.java).baseUrl,
            arrayOf(
                ChuckerInterceptor(context),
                ResultInterceptor(),
            ),
        )
    }

    override fun dependencies(): Set<Class<out Initializer<*>>> =
        setOf(LoggerInitTask::class.java, ConfigInjectInitTask::class.java)
}
