package com.yikwing.extension.coroutines

import jakarta.inject.Qualifier

/**
 * Dispatcher 限定符
 * 用于区分不同的 CoroutineDispatcher
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(
    val niaDispatcher: NiaDispatchers,
)

/**
 * Dispatcher 类型枚举
 */
enum class NiaDispatchers {
    /** 默认调度器，用于 CPU 密集型任务 */
    Default,

    /** IO 调度器，用于网络请求、文件读写、数据库操作 */
    IO,
}
