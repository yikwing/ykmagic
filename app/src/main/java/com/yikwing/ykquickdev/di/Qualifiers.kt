package com.yikwing.ykquickdev.di

import org.koin.core.annotation.Qualifier

/**
 * 应用 Koin Qualifier 注解
 *
 * 用于区分相同原始类型的多个依赖实例（如多个 `DataStore<T>`、多个 `String`），
 * 支持 Koin 编译期安全验证：provider 与注入点的 qualifier 不匹配会直接编译失败。
 * 使用 `@Retention(BINARY)` 即可满足编译器插件处理，无运行时开销。
 */

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UserPreferencesStore

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppSettingsStore

/** API 基础 URL */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseUrl

/** Debug 模式标志 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DebugFlag
