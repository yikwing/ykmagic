package com.yikwing.network.log

import okhttp3.Headers

/**
 * HTTP 请求日志条目，记录完整的请求/响应信息
 */
data class LogEntry(
    val id: Int,
    val startTime: Long,
    var endTime: Long? = null,
    var duration: Long? = null,
    // 请求信息
    val requestMethod: String,
    val requestUrl: String,
    val requestHeaders: Headers,
    val requestBody: String? = null,
    // 响应信息
    var responseCode: Int? = null,
    var responseMessage: String? = null,
    var responseHeaders: Headers? = null,
    var responseBody: String? = null,
    var exception: Throwable? = null,
)
