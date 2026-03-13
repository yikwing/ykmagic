package com.yikwing.network

import org.koin.core.annotation.Qualifier

/**
 * 网络模块 Koin Qualifier 注解
 *
 * 用于区分相同类型的不同依赖实例，支持 Koin 编译时安全验证。
 * 使用 `@Retention(BINARY)` 支持 KSP 处理，无运行时开销。
 */

/** API 基础 URL */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseUrl

/** Chucker 网络调试拦截器（仅 Debug） */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ChuckerInterceptorQualifier

/** 请求头拦截器 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class HeaderInterceptorQualifier

/** OkLog 日志拦截器 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OkLogInterceptorQualifier

/** Debug 模式标志 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DebugFlag