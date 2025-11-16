package com.yikwing.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import kotlin.math.pow

class RetryInterceptor(
    private val maxRetries: Int = 3, // 定义为最大重试次数
    private val initialDelay: Long = 1000, // 初始延迟
    private val maxDelay: Long = 30000, // 最大延迟, 默认30秒
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response? = null
        var lastException: IOException? = null

        // 总尝试次数 = 1 (初次) + maxRetries (重试次数)
        for (attempt in 0..maxRetries) {
            try {
                // 关键点: 每次重试前关闭上一次的响应体, 防止泄漏
                response?.close()

                response = chain.proceed(request)

                // 如果成功, 或遇到不应重试的客户端错误(4xx), 则直接返回
                if (response.isSuccessful || !isServerError(response)) {
                    return response
                }
            } catch (e: IOException) {
                lastException = e
                // 如果是IO异常, 则继续重试
            }

            // 如果是最后一次尝试, 则不再等待
            if (attempt >= maxRetries) {
                break
            }

            // 计算延迟, 包含指数回退和抖动
            var nextDelay = initialDelay * (2.0.pow(attempt)).toLong()
            // 增加 +/- 20% 的抖动
            val jitter = (nextDelay * 0.4 * Math.random() - nextDelay * 0.2).toLong()
            nextDelay += jitter
            // 确保延迟不超过最大值
            if (nextDelay > maxDelay) {
                nextDelay = maxDelay
            }
            // 确保延迟不为负
            if (nextDelay < 0) {
                nextDelay = 0
            }

            // 记录重试日志
            if (attempt > 0) {
                val reason = lastException?.message ?: "服务器错误 ${response?.code}"
                Log.w(
                    "RetryInterceptor",
                    "重试请求 [${request.url}] - 尝试 $attempt/$maxRetries, 原因: $reason, 下次延迟: ${nextDelay}ms"
                )
            }

            try {
                Thread.sleep(nextDelay)
            } catch (ie: InterruptedException) {
                Thread.currentThread().interrupt()
                throw IOException("Retry interrupted", ie)
            }
        }

        // 所有尝试结束后, 如果仍有异常, 则抛出最后的异常
        throw lastException
            ?: IOException("Request failed after $maxRetries retries, and no response was received.")
    }

    private fun isServerError(response: Response): Boolean {
        // 5xx 范围的错误码表示服务器端错误, 适合重试
        return response.code in 500..599
    }
}
