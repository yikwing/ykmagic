package com.yikwing.ykquickdev.task

import android.content.Context
import android.util.Log
import com.yikwing.extension.coroutines.ScopesModule.DEFAULT_SCOPE
import com.yikwing.network.ApiConfig
import com.yikwing.proxy.startup.Initializer
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

class NetworkInitTask :
    Initializer<Unit>,
    KoinComponent {
    private val applicationScope: CoroutineScope by inject(named(DEFAULT_SCOPE))
    private val httpClient: HttpClient by inject()

    override fun create(context: Context) {
        ApiConfig.errorCodeChecker = { it != 0 }

        // 使用注入的 Application Scope 预热 HttpClient
        applicationScope.launch {
            runCatching {
                withTimeout(5000) {
                    httpClient
                }
            }.onFailure { e ->
                Log.e("NetworkInitTask", "HttpClient 预热失败", e) // 使用日志框架
            }
        }
    }

    override fun dependencies(): Set<Class<out Initializer<*>>> =
        setOf(
            LoggerInitTask::class.java,
            ConfigInjectInitTask::class.java,
        )
}
