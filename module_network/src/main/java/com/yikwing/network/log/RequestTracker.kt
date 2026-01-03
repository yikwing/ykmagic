package com.yikwing.network.log

import android.util.Log
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

        Log.i(TAG, LogFormatter.format(entry))
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

        Log.e(TAG, LogFormatter.format(entry))
    }
}
