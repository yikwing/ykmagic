package com.yikwing.network

import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * 用于标记 BaseUrl 的限定符
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseUrl

/**
 * 用于标记应用层拦截器列表
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationInterceptors

/**
 * 用于标记网络层拦截器列表
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NetworkInterceptors

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
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val TIMEOUT_MS = 30_000L
    private const val CONNECT_TIMEOUT_MS = 15_000L
    private const val READ_WRITE_TIMEOUT_MS = 15_000L

    /**
     * 提供共享的 OkHttpClient，支持注入自定义拦截器
     *
     * 拦截器执行顺序:
     * 1. OkLogInterceptor (日志)
     * 2. 应用层拦截器 (Header、Auth 等)
     * 3. RetryInterceptor (重试)
     * 4. 网络层拦截器
     */
    @Singleton
    @Provides
    fun provideOkHttpClient(
        @ApplicationInterceptors applicationInterceptors: List<@JvmSuppressWildcards Interceptor>,
        @NetworkInterceptors networkInterceptors: List<@JvmSuppressWildcards Interceptor>,
    ): OkHttpClient =
        OkHttpClient
            .Builder()
            .apply {
                // 添加日志拦截器
                addInterceptor(OkLogInterceptor())

                // 添加应用层拦截器
                applicationInterceptors.forEach { addInterceptor(it) }

                // 添加重试拦截器
                addInterceptor(RetryInterceptor())

                // 添加网络层拦截器
                networkInterceptors.forEach { addNetworkInterceptor(it) }

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
    @Provides
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
    @Provides
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
