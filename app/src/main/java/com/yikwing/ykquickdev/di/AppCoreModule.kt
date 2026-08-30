package com.yikwing.ykquickdev.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.annotation.Module
import org.koin.core.annotation.Qualifier
import org.koin.core.annotation.Singleton

/**
 * 用于标记应用级 CoroutineScope
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

/**
 * 应用核心模块
 *
 * 提供应用级别的核心依赖：
 * - ApplicationScope：应用级协程作用域（整个应用生命周期）
 * - ActivityHierarchyManager：Activity 生命周期管理（如需要）
 * - CacheManager：内存缓存管理（如需要）
 *
 * 该模块提供的依赖是应用的基础设施，被其他模块依赖。
 */
@Module
object AppCoreModule {
    /**
     * 提供应用级 CoroutineScope
     * - 使用 SupervisorJob：子协程失败不影响其他协程
     * - 使用 Dispatchers.Main：默认在主线程执行
     * - 生命周期：整个应用生命周期
     */
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
}
