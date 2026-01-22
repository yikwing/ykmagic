package com.yikwing.extension.coroutines

import jakarta.inject.Named
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module

/**
 * 协程作用域模块
 * 提供不同用途的 CoroutineScope
 *
 * 使用说明：
 * - provideApplicationScope: Application 级别的 CoroutineScope（使用 @Named("DefaultScope")），使用 Default Dispatcher
 * - provideIoScope: IO 专用的 CoroutineScope（使用 @Named("IoScope")），使用 IO Dispatcher
 *
 * 注意事项：
 * - 这些 Scope 的生命周期与 Application 相同，不会自动取消
 * - 仅用于应用全局的后台任务（如预热、数据同步）
 * - 业务层推荐注入 CoroutineDispatcher，配合 viewModelScope/lifecycleScope 使用
 */
@Module
@Configuration
object ScopesModule {
    /**
     * Scope 名称常量，避免字符串拼写错误
     */
    const val DEFAULT_SCOPE = "DefaultScope"
    const val IO_SCOPE = "IoScope"

    /**
     * 提供 Application 级别的 CoroutineScope
     * 使用 Default Dispatcher，适合 CPU 密集型任务
     *
     * 注入方式（必须使用 @Named qualifier）：
     * ```kotlin
     * // 在构造函数中注入
     * class NetworkInitTask @Inject constructor(
     *     @Named(value = DEFAULT_SCOPE) private val applicationScope: CoroutineScope
     * )
     *
     * // 在 KoinComponent 中注入
     * import org.koin.core.qualifier.named
     * class MyClass : KoinComponent {
     *     private val appScope: CoroutineScope by inject(named(DEFAULT_SCOPE))
     * }
     * ```
     */
    @Singleton
    @Named(value = DEFAULT_SCOPE)
    fun provideApplicationScope(
        @Dispatcher(NiaDispatchers.Default) dispatcher: CoroutineDispatcher,
    ): CoroutineScope =
        CoroutineScope(
            SupervisorJob() + dispatcher + CoroutineName(DEFAULT_SCOPE),
        )

    /**
     * 提供 IO 专用的 CoroutineScope
     * 使用 IO Dispatcher，适合网络请求、文件读写、数据库操作
     *
     * 注入方式（必须使用 @Named qualifier）：
     * ```kotlin
     * // 在构造函数中注入
     * class MyRepository @Inject constructor(
     *     @Named(value = IO_SCOPE) private val ioScope: CoroutineScope
     * )
     *
     * // 在 KoinComponent 中注入
     * import org.koin.core.qualifier.named
     * class MyClass : KoinComponent {
     *     private val ioScope: CoroutineScope by inject(named(IO_SCOPE))
     * }
     * ```
     */
    @Singleton
    @Named(value = IO_SCOPE)
    fun provideIoScope(
        @Dispatcher(NiaDispatchers.IO) dispatcher: CoroutineDispatcher,
    ): CoroutineScope =
        CoroutineScope(
            SupervisorJob() + dispatcher + CoroutineName(IO_SCOPE),
        )
}
