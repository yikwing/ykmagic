package com.yikwing.network.log

import android.util.Log
import okhttp3.Headers
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import java.nio.charset.Charset
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * HTTP 请求追踪管理器
 *
 * 线程安全，使用 ConcurrentHashMap + AtomicInteger 实现
 */
object RequestTracker {
    private const val TAG = "OkLog"

    private val requestIdCounter = AtomicInteger(0)
    private val pendingLogs = ConcurrentHashMap<Int, LogEntry>()

    /**
     * 开始追踪请求，创建并存储 LogEntry
     * @return 请求的唯一 ID
     */
    fun startRequest(request: Request): Int {
        val id = requestIdCounter.incrementAndGet()

        val requestBodyString = request.body?.let { body ->
            val buffer = Buffer()
            body.writeTo(buffer)
            val charset = body.contentType()?.charset(Charset.forName("UTF-8"))
                ?: Charset.forName("UTF-8")
            buffer.readString(charset)
        }

        val logEntry = LogEntry(
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
     * 记录响应信息并输出日志
     */
    fun completeRequest(id: Int, response: Response) {
        val entry = pendingLogs.remove(id) ?: return

        val responseBodyString = response.body.let { body ->
            val source = body.source()
            source.request(Long.MAX_VALUE)
            val buffer = source.buffer.clone()
            val charset = body.contentType()?.charset(Charset.forName("UTF-8"))
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

        Log.i(TAG, formatEntry(entry))
    }

    /**
     * 记录请求异常并输出日志
     */
    fun failRequest(id: Int, e: Throwable) {
        val entry = pendingLogs.remove(id) ?: return

        entry.apply {
            endTime = System.currentTimeMillis()
            duration = endTime!! - startTime
            exception = e
        }

        Log.e(TAG, formatEntry(entry))
    }

    private fun formatEntry(entry: LogEntry): String = buildString {
        val durationInfo = entry.duration?.let { "${it}ms" } ?: "N/A"

        append("---[ OkHttp ID: #${entry.id} | ${statusEmoji(entry.responseCode)} | duration: $durationInfo ]---------------------------------\n")

        // 请求信息
        append("➡️ Request: ${entry.requestMethod} ${entry.requestUrl}")
        append("\n₍^. .^₎⟆ \n${entry.requestHeaders}")
        if (!entry.requestBody.isNullOrBlank()) {
            append("\nBody:\n").append(entry.requestBody).append("\n")
        }

        // 响应信息
        when {
            entry.responseCode != null -> {
                append("\n⬅️ Response: ${entry.responseCode} ${entry.responseMessage ?: ""}\n")
                if (!entry.responseBody.isNullOrBlank() && isJsonResponse(entry.responseHeaders)) {
                    append(entry.responseBody).append("\n")
                }
            }
            entry.exception != null -> {
                append("\n❗️ Error: ${entry.exception?.message}\n")
            }
            else -> {
                append("... No network response ...\n")
            }
        }

        append("------------------------------------------------------------------\n\n")
    }

    private fun statusEmoji(code: Int?): String = when (code) {
        null -> "⏱️"
        in 200..299 -> "✅"
        in 300..399 -> "➡️"
        in 400..499 -> "⚠️"
        else -> "❌"
    }

    private fun isJsonResponse(headers: Headers?): Boolean =
        headers?.get("Content-Type")?.contains("application/json", ignoreCase = true) == true
}

/**
 * HTTP 请求日志条目
 */
private data class LogEntry(
    val id: Int,
    val startTime: Long,
    var endTime: Long? = null,
    var duration: Long? = null,
    val requestMethod: String,
    val requestUrl: String,
    val requestHeaders: Headers,
    val requestBody: String? = null,
    var responseCode: Int? = null,
    var responseMessage: String? = null,
    var responseHeaders: Headers? = null,
    var responseBody: String? = null,
    var exception: Throwable? = null,
)
