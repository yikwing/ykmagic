package com.yikwing.ykquickdev.di

import android.app.Application
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.yikwing.config.YkConfigManager
import com.yikwing.network.BaseUrl
import com.yikwing.network.ChuckerInterceptorQualifier
import com.yikwing.network.DebugFlag
import com.yikwing.network.HeaderInterceptorQualifier
import com.yikwing.network.OkLogInterceptorQualifier
import com.yikwing.network.log.OkLogInterceptor
import com.yikwing.ykquickdev.BuildConfig
import com.yikwing.ykquickdev.HeaderInterceptor
import jakarta.inject.Singleton
import okhttp3.Interceptor
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module

/**
 * 应用网络模块
 *
 * 提供网络层依赖：
 * - BaseUrl：API 基础 URL（从 YkConfigManager 读取配置）
 * - DebugFlag：调试标志（从 BuildConfig 读取）
 * - Interceptor：网络拦截器
 *   - ChuckerInterceptor：网络调试工具（仅 Debug 版本）
 *   - HeaderInterceptor：请求头拦截器
 *   - OkLogInterceptor：日志拦截器
 *
 * 依赖：
 * - module_network 提供的 NetworkScanModule（Ktor HttpClient 配置）
 * - YkConfigManager（配置管理）
 */
@Module
@Configuration
object AppNetworkModule {
    /**
     * 提供 BaseUrl
     * 从 YkConfigManager 获取配置
     */
    @Singleton
    @BaseUrl
    fun provideBaseUrl(): String = YkConfigManager.config.baseUrl

    /**
     * 提供 Debug 标志
     */
    @Singleton
    @DebugFlag
    fun provideDebug(): Boolean = BuildConfig.DEBUG

    /**
     * 提供 ChuckerInterceptor
     */
    @Singleton
    @ChuckerInterceptorQualifier
    fun provideChuckerInterceptor(application: Application): Interceptor = ChuckerInterceptor(application)

    /**
     * 提供 HeaderInterceptor
     */
    @Singleton
    @HeaderInterceptorQualifier
    fun provideHeaderInterceptor(headerInterceptor: HeaderInterceptor): Interceptor = headerInterceptor

    /**
     * 提供 OkLogInterceptor
     */
    @Singleton
    @OkLogInterceptorQualifier
    fun provideOkLogInterceptor(okLogInterceptor: OkLogInterceptor): Interceptor = okLogInterceptor
}
