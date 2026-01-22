package com.yikwing.extension.coroutines

import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module

/**
 * Dispatcher 模块
 * 提供不同用途的 CoroutineDispatcher
 *
 * 使用示例：
 * ```kotlin
 * class MyRepository @Inject constructor(
 *     @Dispatcher(NiaDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
 * )
 * ```
 */
@Module
@Configuration
object DispatchersModule {
    /**
     * 提供 IO Dispatcher
     * 用于：网络请求、文件读写、数据库操作
     */
    @Singleton
    @Dispatcher(NiaDispatchers.IO)
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    /**
     * 提供 Default Dispatcher
     * 用于：CPU 密集型任务、数据处理
     */
    @Singleton
    @Dispatcher(NiaDispatchers.Default)
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
}
