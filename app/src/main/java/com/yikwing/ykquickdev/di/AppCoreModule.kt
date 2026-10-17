package com.yikwing.ykquickdev.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

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
    @Singleton
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    /**
     * 提供应用级 CoroutineScope
     * - 使用 SupervisorJob：子协程失败不影响其他协程
     * - 使用注入的 Default 调度器：应用级任务不应占用主线程，需要更新 UI 时由调用方切换
     * - 生命周期：整个应用生命周期
     */
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(
        @DefaultDispatcher dispatcher: CoroutineDispatcher,
    ): CoroutineScope = CoroutineScope(SupervisorJob() + dispatcher)
}
