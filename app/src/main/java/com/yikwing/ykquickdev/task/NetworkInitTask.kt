package com.yikwing.ykquickdev.task

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.yikwing.config.YkConfigManager
import com.yikwing.network.RetrofitFactory
import com.yikwing.proxy.startup.Initializer
import com.yikwing.ykquickdev.NetworkConfig
import com.yikwing.ykquickdev.ResultInterceptor

class NetworkInitTask : Initializer<Unit> {
    override fun create(context: Context) {
        RetrofitFactory.instance.setup(
            baseUrl = YkConfigManager.getConfig(NetworkConfig::class.java).baseUrl,
            applicationInterceptor = listOf(
                ChuckerInterceptor(context),
                ResultInterceptor(),
            ),
            networkInterceptor = emptyList(),
        )
    }

    override fun dependencies(): Set<Class<out Initializer<*>>> =
        setOf(LoggerInitTask::class.java, ConfigInjectInitTask::class.java)
}
