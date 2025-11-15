package com.yikwing.network

import android.util.Log
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import java.io.IOException
import java.nio.charset.Charset
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

// 完整的日志 entry，包含请求和响应的所有信息
data class LogEntry(
    val id: Int, // 请求唯一ID
    val startTime: Long, // 请求开始时间
    var endTime: Long? = null, // 请求结束时间
    var duration: Long? = null, // 耗时
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
    var exception: Throwable? = null, // 用于记录请求过程中的异常
) {
    // 助手函数，判断响应体是否是JSON (基于 Content-Type)
    fun isJsonResponse(): Boolean {
        val contentType = responseHeaders?.get("Content-Type")
        return contentType?.contains("application/json", ignoreCase = true) == true
    }

    // 将LogEntry格式化为可读的字符串，用于最终打印
    fun formatLog(): String {
        val sb = StringBuilder()
        val statusEmoji =
            when (responseCode) {
                null -> "⏱️" // Still in progress or failed before response
                in 200..299 -> "✅" // Success
                in 300..399 -> "➡️" // Redirect
                in 400..499 -> "⚠️" // Client Error
                else -> "❌" // Server Error or other
            }

        val durationInfo = duration?.let { "${it}ms" } ?: "N/A"

        sb.append("---[ OkHttp ID: #$id | $statusEmoji | duration: $durationInfo ]---------------------------------\n")

        // 请求信息
        sb.append("➡️ Request: $requestMethod $requestUrl")
        sb.append("\n₍^. .^₎⟆ \n$requestHeaders")
        if (!requestBody.isNullOrBlank()) {
            sb.append("\nBody:\n").append(requestBody).append("\n")
        }

        // 响应信息
        if (responseCode != null) {
            sb.append("\n⬅️ Response: $responseCode ${responseMessage ?: ""}\n")
            if (!responseBody.isNullOrBlank()) {
                if (isJsonResponse()) {
                    sb.append(responseBody).append("\n")
                }
            }
        } else if (exception != null) {
            sb.append("\n❗️ Error: ${exception?.message}\n")
            exception?.printStackTrace() // 打印堆栈信息帮助调试
        } else {
            sb.append("... No network response ...\n")
        }

        sb.append("------------------------------------------------------------------\n\n")
        return sb.toString()
    }
}

object RequestTracker {
    private val requestIdCounter = AtomicInteger(0)

    // 存储 LogEntry，key 为请求ID
    private val pendingLogs = ConcurrentHashMap<Int, LogEntry>()

    /**
     * 开始追踪一个请求，创建并存储 LogEntry。
     * @return 请求的唯一ID
     */
    fun startRequest(request: Request): Int {
        val id = requestIdCounter.incrementAndGet()

        val requestBodyString =
            request.body?.let { body ->
                val buffer = Buffer()
                body.writeTo(buffer)
                val charset =
                    body.contentType()?.charset(Charset.forName("UTF-8"))
                        ?: Charset.forName("UTF-8")
                buffer.readString(charset)
            }

        val logEntry =
            LogEntry(
                id = id,
                startTime = System.currentTimeMillis(),
                requestMethod = request.method,
                requestUrl = request.url.toString(),
                requestHeaders = request.headers,
                requestBody = requestBodyString,
            )
        pendingLogs[id] = logEntry
        return id
    }

    /**
     * 记录响应信息并触发日志打印。
     * @param id 请求ID
     * @param response OkHttp的响应对象
     */
    fun completeRequest(
        id: Int,
        response: Response,
    ) {
        val entry = pendingLogs.remove(id) ?: return // 如果找不到，说明已经处理过或ID错误

        val responseBodyString =
            response.body.let { body ->
                val source = body.source()
                source.request(Long.MAX_VALUE)
                val buffer = source.buffer.clone() // 克隆buffer，确保原body仍可被消费
                val charset =
                    body.contentType()?.charset(Charset.forName("UTF-8"))
                        ?: Charset.forName("UTF-8")
                buffer.readString(charset)
            }

        entry.apply {
            endTime = System.currentTimeMillis()
            duration = endTime!! - startTime
            responseCode = response.code
            responseMessage = response.message
            responseHeaders = response.headers
            responseBody = responseBodyString
        }

        // 🎯 核心：在这里一次性打印格式化的完整日志
        Log.i("OkLog", entry.formatLog())
    }

    /**
     * 记录请求过程中的异常并触发日志打印。
     * @param id 请求ID
     * @param e 发生的异常
     */
    fun failRequest(
        id: Int,
        e: Throwable,
    ) {
        val entry = pendingLogs.remove(id) ?: return

        entry.apply {
            endTime = System.currentTimeMillis()
            duration = endTime!! - startTime
            exception = e
        }

        // 打印异常日志
        Log.e("OkLog", entry.formatLog())
    }
}

class OkLogInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestId = RequestTracker.startRequest(request) // 开始追踪请求

        try {
            val response = chain.proceed(request) // 执行请求
            RequestTracker.completeRequest(requestId, response) // 请求成功，记录响应并打印
            return response
        } catch (e: Exception) {
            RequestTracker.failRequest(requestId, e) // 请求失败，记录异常并打印
            throw e // 重新抛出异常，让上层业务逻辑处理
        }
    }
}
