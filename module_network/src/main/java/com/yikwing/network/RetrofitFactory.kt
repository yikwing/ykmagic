package com.yikwing.network

import android.util.Log
import com.yikwing.network.log.OkLogInterceptor
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.client.request.accept
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import jakarta.inject.Qualifier
import jakarta.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import java.util.concurrent.TimeUnit

/**
 * 用于标记 BaseUrl 的限定符
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseUrl

/**
 * 用于标记 ChuckerInterceptor
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ChuckerInterceptorQualifier

/**
 * 用于标记 HeaderInterceptor
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class HeaderInterceptorQualifier

/**
 * 用于标记 OkLogInterceptor
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OkLogInterceptorQualifier

/**
 * 用于标记 Debug 标志
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DebugFlag

/**
 * Ktor 网络模块配置
 *
 * 支持:
 * - 复用已有的 OkHttp 拦截器 (RetryInterceptor, OkLogInterceptor 等)
 * - 可配置的 BaseUrl
 * - JSON 序列化 (kotlinx.serialization)
 * - 超时配置
 * - Debug 日志
 */
@Module
@Configuration
object NetworkModule {
    private const val TIMEOUT_MS = 30_000L
    private const val CONNECT_TIMEOUT_MS = 15_000L
    private const val READ_WRITE_TIMEOUT_MS = 15_000L

    /**
     * 提供共享的 OkHttpClient，支持注入自定义拦截器
     *
     * 拦截器执行顺序:
     * 1. 应用层拦截器 (Header、Auth 等)
     * 2. RetryInterceptor (重试)
     * 3. OkLogInterceptor (日志) - 记录最终请求/响应
     * 4. 网络层拦截器
     */
    @Singleton
    fun provideOkHttpClient(
        @ChuckerInterceptorQualifier chuckerInterceptor: Interceptor,
        @HeaderInterceptorQualifier headerInterceptor: Interceptor,
        @OkLogInterceptorQualifier okLogInterceptor: Interceptor,
        @DebugFlag debug: Boolean,
    ): OkHttpClient =
        OkHttpClient
            .Builder()
            .apply {
                // 添加应用层拦截器

                // 仅在 Debug 模式添加 Chucker,避免 Release 版本的性能开销
                if (debug) {
                    addInterceptor(chuckerInterceptor)
                }
                addInterceptor(headerInterceptor)

                // 添加重试拦截器
                addInterceptor(RetryInterceptor())

                // 添加日志拦截器 (放在最后，记录最终请求/响应)
                addInterceptor(okLogInterceptor)

                // 超时配置
                callTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                connectTimeout(CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                readTimeout(READ_WRITE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                writeTimeout(READ_WRITE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            }.build()

    /**
     * 提供 JSON 序列化配置
     */
    @Singleton
    fun provideJson(): Json =
        Json {
            isLenient = true
            ignoreUnknownKeys = true
            coerceInputValues = true
            explicitNulls = false
        }

    /**
     * 提供 Ktor HttpClient
     *
     * 特性:
     * - 使用 OkHttp 引擎，复用已配置的 OkHttpClient
     * - 支持 kotlinx.serialization
     * - 支持可配置的 BaseUrl
     */
    @Singleton
    fun provideHttpClient(
        okHttpClient: OkHttpClient,
        json: Json,
        @BaseUrl baseUrl: String,
    ): HttpClient =
        HttpClient(OkHttp) {
            // 使用预配置的 OkHttpClient (包含所有拦截器)
            engine {
                preconfigured = okHttpClient
            }

            // JSON 序列化配置
            install(ContentNegotiation) {
                json(json)
            }

            // 超时配置 (Ktor 层面)
            install(HttpTimeout) {
                requestTimeoutMillis = TIMEOUT_MS
                connectTimeoutMillis = CONNECT_TIMEOUT_MS
                socketTimeoutMillis = READ_WRITE_TIMEOUT_MS
            }

            // 默认请求配置
            install(DefaultRequest) {
                url(baseUrl)
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
            }

//            // Debug 日志配置
//            if (BuildConfig.DEBUG) {
//                install(Logging) {
//                    logger =
//                        object : Logger {
//                            override fun log(message: String) {
//                                Log.d("Ktor", message)
//                            }
//                        }
//                    level = LogLevel.BODY
//                }
//            }

            // 响应观察器
            install(ResponseObserver) {
                onResponse { response ->
                    Log.d("HTTP", "Status: ${response.status.value}")
                }
            }
        }
}
