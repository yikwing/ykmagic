package com.yikwing.network

import android.util.Log

/**
 * 全局 API 配置对象
 *
 * 用于配置网络请求的全局行为，包括错误码检查策略等
 */
object ApiConfig {
    /**
     * 全局错误码检查器
     *
     * 用于判断响应的 errorCode 是否表示失败
     *
     * @param errorCode 响应中的错误码
     * @return true 表示失败（会抛出 ApiException），false 表示成功
     *
     * 默认行为：errorCode != 0 表示失败
     *
     * 使用示例：
     * ```
     * // 示例 1: 某些 API 使用 200 表示成功
     * ApiConfig.errorCodeChecker = { it != 200 }
     *
     * // 示例 2: 0 和 1 都表示成功
     * ApiConfig.errorCodeChecker = { it !in setOf(0, 1) }
     *
     * // 示例 3: 恢复默认行为
     * ApiConfig.errorCodeChecker = { it != 0 }
     * ```
     */

    @Volatile
    var errorCodeChecker: (Int) -> Boolean = { it != 0 }

    /**
     * 全局错误日志输出器
     *
     * [requestStateFlow] / [requestResult] 捕获到异常时调用，默认写 [Log].e
     *
     * 替换它可以关闭库内日志、接入自有日志框架，或在 JVM 单元测试中避免
     * `android.util.Log` 未 mock 抛出的 "not mocked" 异常
     *
     * @param tag 日志标签，取值为触发日志的入口函数名
     * @param message 日志内容
     * @param throwable 触发日志的异常
     *
     * 使用示例：
     * ```
     * // 示例 1: 关闭库内日志
     * ApiConfig.logger = { _, _, _ -> }
     *
     * // 示例 2: 接入 Timber
     * ApiConfig.logger = { tag, message, throwable -> Timber.tag(tag).e(throwable, message) }
     * ```
     */
    @Volatile
    var logger: (tag: String, message: String, throwable: Throwable?) -> Unit =
        { tag, message, throwable -> Log.e(tag, message, throwable) }
}
