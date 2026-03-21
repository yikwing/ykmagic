package com.yikwing.network

import kotlinx.serialization.json.Json
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

/**
 * 网络模块基础配置
 *
 * 提供 JSON 序列化等基础组件，不依赖 app 层的 qualifier 绑定。
 * OkHttpClient 和 HttpClient 由 AppNetworkModule 组装。
 */
@Module
@Configuration
@ComponentScan("com.yikwing.network")
object NetworkModule {
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
}