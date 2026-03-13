package com.yikwing.ykquickdev.di

import android.app.Application
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.yikwing.config.YkConfigManager
import com.yikwing.network.BaseUrl
import com.yikwing.network.ChuckerInterceptorQualifier
import com.yikwing.network.DebugFlag
import com.yikwing.network.HeaderInterceptorQualifier
import com.yikwing.network.OkLogInterceptorQualifier
import com.yikwing.network.RetryInterceptor
import com.yikwing.network.log.OkLogInterceptor
import com.yikwing.ykquickdev.BuildConfig
import com.yikwing.ykquickdev.HeaderInterceptor
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.client.request.accept
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton
import java.util.concurrent.TimeUnit

/**
 * 应用网络模块
 *
 * 组装完整的网络层依赖图，提供 OkHttpClient 和 Ktor HttpClient。
 *
 * ## 拦截器执行顺序
 * 1. ChuckerInterceptor (Debug only)
 * 2. HeaderInterceptor
 * 3. RetryInterceptor
 * 4. OkLogInterceptor
 * 5. 网络层拦截器 (OkHttp 内置)
 */
@Module
@Configuration
object AppNetworkModule {
    private const val TIMEOUT_MS = 30_000L
    private const val CONNECT_TIMEOUT_MS = 15_000L
    private const val READ_WRITE_TIMEOUT_MS = 15_000L

    /** API 基础 URL（从 YkConfigManager 读取） */
    @Singleton
    @BaseUrl
    fun provideBaseUrl(): String = YkConfigManager.config.baseUrl

    /** Debug 模式标志（从 BuildConfig 读取） */
    @Singleton
    @DebugFlag
    fun provideDebug(): Boolean = BuildConfig.DEBUG

    /** Chucker 网络调试拦截器（仅 Debug） */
    @Singleton
    @ChuckerInterceptorQualifier
    fun provideChuckerInterceptor(application: Application): Interceptor = ChuckerInterceptor(application)

    /** 请求头拦截器 */
    @Singleton
    @HeaderInterceptorQualifier
    fun provideHeaderInterceptor(headerInterceptor: HeaderInterceptor): Interceptor = headerInterceptor

    /** OkLog 日志拦截器 */
    @Singleton
    @OkLogInterceptorQualifier
    fun provideOkLogInterceptor(okLogInterceptor: OkLogInterceptor): Interceptor = okLogInterceptor

    /**
     * 共享的 OkHttpClient
     *
     * 超时配置：call 30s / connect 15s / read 15s / write 15s
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
                if (debug) addInterceptor(chuckerInterceptor)
                addInterceptor(headerInterceptor)
                addInterceptor(RetryInterceptor())
                addInterceptor(okLogInterceptor)

                callTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                connectTimeout(CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                readTimeout(READ_WRITE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                writeTimeout(READ_WRITE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            }.build()

    /**
     * Ktor HttpClient
     *
     * 使用 OkHttp 引擎，复用已配置的 OkHttpClient。
     * 默认配置：BaseUrl + JSON Content-Type
     */
    @Singleton
    fun provideHttpClient(
        okHttpClient: OkHttpClient,
        json: Json,
        @BaseUrl baseUrl: String,
    ): HttpClient =
        HttpClient(OkHttp) {
            engine { preconfigured = okHttpClient }

            install(ContentNegotiation) { json(json) }

            install(DefaultRequest) {
                url(baseUrl)
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
            }

            install(ResponseObserver) {
                onResponse { /* 日志由 OkLogInterceptor 记录 */ }
            }
        }
}
