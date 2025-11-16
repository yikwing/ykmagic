package com.yikwing.ykquickdev

import com.yikwing.network.BaseHeaderInterceptor

class HeaderInterceptor : BaseHeaderInterceptor() {
    // 缓存头部信息，避免每次请求都创建新的 Map 对象
    private val cachedHeaders: Map<String, String> = mapOf(
        "version" to BuildConfig.VERSION_NAME,
        "User-Agent" to "${BuildConfig.APPLICATION_ID}_${BuildConfig.VERSION_NAME}",
    )

    override fun headerList(): Map<String, String> = cachedHeaders
}
