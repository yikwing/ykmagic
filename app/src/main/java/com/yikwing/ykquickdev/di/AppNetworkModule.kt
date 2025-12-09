package com.yikwing.ykquickdev.di

import android.app.Application
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.yikwing.config.NetworkConfig
import com.yikwing.config.YkConfigManager
import com.yikwing.network.ApplicationInterceptors
import com.yikwing.network.BaseUrl
import com.yikwing.network.NetworkInterceptors
import com.yikwing.ykquickdev.HeaderInterceptor
import okhttp3.Interceptor
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import javax.inject.Singleton

/**
 * App 模块的网络配置
 *
 * 提供:
 * - 自定义 BaseUrl (从 YkConfigManager 获取)
 * - 自定义应用层拦截器 (Chucker + HeaderInterceptor)
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
    fun provideBaseUrl(): String = YkConfigManager.getConfig(NetworkConfig::class.java).baseUrl

    /**
     * 提供应用层拦截器列表
     * - ChuckerInterceptor: Debug 模式下的网络抓包工具
     * - HeaderInterceptor: 添加自定义 Header
     */
    @Singleton
    @ApplicationInterceptors
    fun provideApplicationInterceptors(application: Application): List<Interceptor> =
        listOf(
            ChuckerInterceptor(application),
            HeaderInterceptor(),
        )

    /**
     * 提供网络层拦截器列表
     */
    @Singleton
    @NetworkInterceptors
    fun provideNetworkInterceptors(): List<Interceptor> = emptyList()
}
