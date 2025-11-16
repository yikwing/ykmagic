package com.yikwing.network

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
}
