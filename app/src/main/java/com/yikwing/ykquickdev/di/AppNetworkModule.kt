package com.yikwing.ykquickdev.di

import com.yikwing.config.YkConfigManager
import com.yikwing.network.BaseUrl
import com.yikwing.network.DebugFlag
import com.yikwing.network.ssl.Https
import com.yikwing.ykquickdev.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

/**
 * 应用网络模块
 *
 * 使用 CIO 引擎，通过 Ktor 原生插件配置网络层：
 * - DefaultRequest: 基础 URL、Content-Type、请求头
 * - HttpRequestRetry: 幂等请求自动重试（指数退避）
 * - Logging: 请求/响应日志（Debug 模式输出 ALL）
 */
@Module
@Configuration
object AppNetworkModule {
    private const val TIMEOUT_MS = 30_000L
    private const val CONNECT_TIMEOUT_MS = 15_000L
    private const val READ_WRITE_TIMEOUT_MS = 15_000L

    @Singleton
    @BaseUrl
    fun provideBaseUrl(): String = YkConfigManager.config.baseUrl

    @Singleton
    @DebugFlag
    fun provideDebug(): Boolean = BuildConfig.DEBUG

    @Singleton
    fun provideHttpClient(
        json: Json,
        @BaseUrl baseUrl: String,
        @DebugFlag debug: Boolean,
    ): HttpClient =
        HttpClient(CIO) {
            engine {
                if (debug) {
                    https {
                        trustManager = Https.UnSafeTrustManager
                    }
                }
            }

            install(HttpTimeout) {
                requestTimeoutMillis = TIMEOUT_MS
                connectTimeoutMillis = CONNECT_TIMEOUT_MS
                socketTimeoutMillis = READ_WRITE_TIMEOUT_MS
            }

            install(ContentNegotiation) { json(json) }

            install(DefaultRequest) {
                url(baseUrl)
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                header("version", BuildConfig.VERSION_NAME)
                header("User-Agent", "${BuildConfig.APPLICATION_ID}_${BuildConfig.VERSION_NAME}")
            }

            install(HttpRequestRetry) {
                maxRetries = 3
                retryOnServerErrors()
                retryOnException(retryOnTimeout = true)
                exponentialDelay()
            }

            install(Logging) {
                logger = Logger.ANDROID
                level = if (debug) LogLevel.ALL else LogLevel.NONE
            }
        }
}
