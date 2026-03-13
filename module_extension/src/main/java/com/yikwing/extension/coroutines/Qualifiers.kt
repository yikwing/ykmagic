package com.yikwing.extension.coroutines

import org.koin.core.annotation.Qualifier

/**
 * 协程调度器 Koin Qualifier 注解
 *
 * 用于区分不同用途的 CoroutineDispatcher 实例。
 * 使用 `@Retention(BINARY)` 以支持 KSP 编译时处理，无运行时反射开销。
 *
 * ## 使用示例
 * ```kotlin
 * @Singleton
 * @Dispatcher(NiaDispatchers.IO)
 * fun provideIODispatcher(): CoroutineDispatcher = Dispatchers.IO
 *
 * class MyRepository(
 *     @Dispatcher(NiaDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
 * ) {
 *     suspend fun fetchData() = withContext(ioDispatcher) { ... }
 * }
 * ```
 *
 * @param niaDispatcher 调度器类型，用于区分不同的调度器实例
 * @see NiaDispatchers
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Dispatcher(
    val niaDispatcher: NiaDispatchers,
)

/**
 * 协程调度器类型枚举
 *
 * 定义应用中使用的调度器类型，遵循 Android 最佳实践。
 *
 * ## 选择指南
 * - **Default**: CPU 密集型任务（计算、数据处理、算法）
 * - **IO**: I/O 密集型任务（网络请求、文件读写、数据库操作）
 *
 * @see kotlinx.coroutines.Dispatchers
 */
enum class NiaDispatchers {
    /**
     * 默认调度器
     *
     * 适用场景：
     * - 复杂计算（排序、过滤、映射）
     * - 数据处理（JSON 解析、图片处理）
     * - 算法执行
     *
     * 底层实现：共享线程池，线程数 = CPU 核心数
     */
    Default,

    /**
     * IO 调度器
     *
     * 适用场景：
     * - 网络请求（API 调用、文件下载）
     * - 文件操作（读写、复制、删除）
     * - 数据库操作（查询、插入、更新）
     *
     * 底层实现：共享线程池，线程数 = max(64, CPU 核心数)
     */
    IO,
}
