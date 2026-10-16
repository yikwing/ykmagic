package com.yikwing.ykquickdev.di

import com.yikwing.config.YkConfigManager
import com.yikwing.network.CommonHeaders
import com.yikwing.network.ssl.Https
import com.yikwing.ykquickdev.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpRequestRetryConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

/**
 * 应用网络模块
 *
 * 提供 Json 序列化配置，并使用 CIO 引擎，通过 Ktor 原生插件组装 HttpClient：
 * - DefaultRequest: 基础 URL、Content-Type、Accept
 * - CommonHeaders: 公共请求头 version / User-Agent（强制覆盖请求级同名头）
 * - HttpRequestRetry: 仅 GET 请求自动重试（指数退避），见 [retryGetOnly]
 * - Logging: 请求/响应日志（Debug 模式输出 ALL）
 */
@Module
object AppNetworkModule {
    private const val TIMEOUT_MS = 30_000L
    private const val CONNECT_TIMEOUT_MS = 15_000L
    private const val READ_WRITE_TIMEOUT_MS = 15_000L

    /**
     * JSON 序列化配置
     *
     * - `isLenient`: 允许非标准 JSON
     * - `ignoreUnknownKeys`: 忽略未知字段
     * - `coerceInputValues`: 强制转换不匹配值
     * - `explicitNulls`: 序列化时省略 null
     */
    @Singleton
    fun provideJson(): Json =
        Json {
            isLenient = true
            ignoreUnknownKeys = true
            coerceInputValues = true
            explicitNulls = false
        }

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
            }

            install(CommonHeaders) {
                headers = {
                    mapOf(
                        "version" to BuildConfig.VERSION_NAME,
                        "User-Agent" to "${BuildConfig.APPLICATION_ID}_${BuildConfig.VERSION_NAME}",
                    )
                }
            }

            install(HttpRequestRetry) { retryGetOnly() }

            install(Logging) {
                logger = Logger.ANDROID
                level = if (debug) LogLevel.ALL else LogLevel.NONE
            }
        }
}

/**
 * Retries only GET requests (3 times, exponential backoff) on 5xx, network errors and timeouts.
 *
 * Ktor's built-in conditions ignore the HTTP method, so non-idempotent requests (POST etc.)
 * would be resent and could duplicate side effects such as a submission. The built-in predicates
 * are wrapped instead of re-implemented because Ktor's timeout detection (which unwraps
 * CancellationException) is private. A single request can still override this with
 * `retry { }`, but that replaces the whole config rather than merging with it.
 */
internal fun HttpRequestRetryConfig.retryGetOnly() {
    retryOnServerErrors(maxRetries = 3)
    retryOnException(maxRetries = 3, retryOnTimeout = true)
    exponentialDelay()

    val retryOnServerError = checkNotNull(retryIf)
    val retryOnError = checkNotNull(retryOnExceptionIf)
    retryIf { request, response ->
        request.method == HttpMethod.Get && retryOnServerError(request, response)
    }
    retryOnExceptionIf { request, cause ->
        request.method == HttpMethod.Get && retryOnError(request, cause)
    }
}
