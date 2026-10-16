package com.yikwing.network

import io.ktor.client.plugins.api.createClientPlugin

/**
 * [CommonHeaders] 插件配置
 */
class CommonHeadersConfig {
    /**
     * 公共请求头提供者，每次请求都会重新调用，可返回运行时变化的值（如 token）。
     *
     * 约束：会在发起请求的协程中被并发调用，必须线程安全且不能阻塞（不要在此做 I/O）；
     * 抛出的异常会直接导致该次请求失败。
     *
     * 默认不添加任何请求头。
     */
    var headers: () -> Map<String, String> = { emptyMap() }
}

/**
 * 公共请求头拦截器：为每个请求注入 [CommonHeadersConfig.headers] 提供的请求头。
 *
 * 同名请求头会被强制覆盖（包括请求级设置的值），保证公共头在所有请求中一致。
 *
 * 使用示例：
 * ```
 * HttpClient(CIO) {
 *     install(CommonHeaders) {
 *         headers = { mapOf("version" to BuildConfig.VERSION_NAME) }
 *     }
 * }
 * ```
 */
val CommonHeaders =
    createClientPlugin("CommonHeaders", ::CommonHeadersConfig) {
        val provideHeaders = pluginConfig.headers

        onRequest { request, _ ->
            provideHeaders().forEach { (name, value) ->
                request.headers[name] = value
            }
        }
    }
