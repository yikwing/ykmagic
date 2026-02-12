package com.yikwing.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import kotlin.math.pow
import kotlin.random.Random

class RetryInterceptor(
    private val maxRetries: Int = 3,
    private val initialDelay: Long = 1000,
    private val maxDelay: Long = 30000,
    private val retryableMethods: Set<String> = IDEMPOTENT_METHODS,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        if (request.method !in retryableMethods) {
            return chain.proceed(request)
        }

        return executeWithRetry(chain, request)
    }

    private fun executeWithRetry(chain: Interceptor.Chain, request: Request): Response {
        var response: Response? = null
        var lastException: IOException? = null

        for (attempt in 0..maxRetries) {
            try {
                response?.close()
                response = chain.proceed(request)

                if (response.isSuccessful || !isRetryableError(response)) {
                    return response
                }
                lastException = null
            } catch (e: IOException) {
                lastException = e
                response = null
            }

            if (attempt >= maxRetries) break

            val delay = calculateDelay(attempt)
            logRetry(request, attempt, lastException, response, delay)

            try {
                Thread.sleep(delay)
            } catch (ie: InterruptedException) {
                Thread.currentThread().interrupt()
                response?.close()
                throw IOException("Retry interrupted", ie)
            }
        }

        response?.close()
        throw lastException
            ?: IOException("Request failed after $maxRetries retries, and no response was received.")
    }

    private fun calculateDelay(attempt: Int): Long {
        val baseDelay = (initialDelay.toDouble() * BACKOFF_BASE.pow(attempt)).toLong()
        val jitter = (baseDelay * JITTER_FACTOR * (2 * Random.nextDouble() - 1)).toLong()
        return (baseDelay + jitter).coerceIn(0L, maxDelay)
    }

    private fun logRetry(
        request: Request,
        attempt: Int,
        exception: IOException?,
        response: Response?,
        delay: Long,
    ) {
        val reason = exception?.message ?: "服务器错误 ${response?.code}"
        Log.w(
            LOG_TAG,
            "重试请求 [${request.url}] - 第 ${attempt + 1}/$maxRetries 次重试, 原因: $reason, 延迟: ${delay}ms",
        )
    }

    private fun isRetryableError(response: Response): Boolean =
        response.code in SERVER_ERROR_RANGE

    companion object {
        private const val LOG_TAG = "RetryInterceptor"
        private const val BACKOFF_BASE = 2.0
        private const val JITTER_FACTOR = 0.2
        private val SERVER_ERROR_RANGE = 500..599

        val IDEMPOTENT_METHODS: Set<String> = setOf("GET", "HEAD", "OPTIONS", "PUT", "DELETE")
        val ALL_METHODS: Set<String> = IDEMPOTENT_METHODS + setOf("POST", "PATCH")
    }
}
