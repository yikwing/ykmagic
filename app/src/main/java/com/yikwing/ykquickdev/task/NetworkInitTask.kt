package com.yikwing.ykquickdev.task

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.yikwing.config.YkConfigManager
import com.yikwing.network.RetrofitFactory
import com.yikwing.proxy.startup.Initializer
import com.yikwing.ykquickdev.HeaderInterceptor
import com.yikwing.ykquickdev.NetworkConfig

class NetworkInitTask : Initializer<Unit> {
    override fun create(context: Context) {
        RetrofitFactory.instance.setup(
            context = context,
            baseUrl = YkConfigManager.getConfig(NetworkConfig::class.java).baseUrl,
            applicationInterceptor =
                listOf(
                    ChuckerInterceptor(context),
                    HeaderInterceptor(),
                ),
            networkInterceptor = emptyList(),
        )
    }

    override fun dependencies(): Set<Class<out Initializer<*>>> = setOf(LoggerInitTask::class.java, ConfigInjectInitTask::class.java)
}
