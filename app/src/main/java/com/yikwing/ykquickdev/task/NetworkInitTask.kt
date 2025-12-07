package com.yikwing.ykquickdev.task

import android.content.Context
import com.yikwing.network.ApiConfig
import com.yikwing.proxy.startup.Initializer

class NetworkInitTask : Initializer<Unit> {
    override fun create(context: Context) {
        // 配置错误码检查策略
        // errorCode != 0 表示业务错误
        ApiConfig.errorCodeChecker = { it != 0 }

        // 网络客户端现在通过 Hilt 依赖注入提供
        // 拦截器配置在 NetworkModule 中完成
        // BaseUrl 通过 @BaseUrl 限定符注入
    }

    override fun dependencies(): Set<Class<out Initializer<*>>> =
        setOf(
            LoggerInitTask::class.java,
            ConfigInjectInitTask::class.java,
        )
}