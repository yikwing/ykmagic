package com.yikwing.ykquickdev.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import java.io.IOException

/**
 * AppNetworkModule 重试策略单元测试
 *
 * 验证 [retryGetOnly]：仅 GET 在 5xx / 异常 / 超时时重试 [MAX_RETRIES] 次，
 * 非幂等方法（POST）只发送一次，避免重复提交。
 * 测试中将退避延迟置 0，只验证重试条件与次数。
 */
class AppNetworkModuleTest {
    private var requestCount = 0

    private fun clientRespondingWith(failure: () -> Nothing?): HttpClient =
        HttpClient(
            MockEngine {
                requestCount++
                failure() ?: respondError(HttpStatusCode.InternalServerError)
            },
        ) {
            install(HttpRequestRetry) {
                retryGetOnly()
                delayMillis { 0 }
            }
        }

    // ==================== 5xx ====================

    @Test
    fun `GET should retry on server error`() =
        runBlocking {
            clientRespondingWith { null }.use { it.get("https://example.com") }

            assertThat(requestCount, `is`(1 + MAX_RETRIES))
        }

    @Test
    fun `POST should not retry on server error`() =
        runBlocking {
            clientRespondingWith { null }.use { it.post("https://example.com") }

            assertThat(requestCount, `is`(1))
        }

    // ==================== Exception ====================

    @Test
    fun `GET should retry on network exception`() =
        runBlocking {
            clientRespondingWith { throw IOException("network down") }.use { client ->
                runCatching { client.get("https://example.com") }
            }

            assertThat(requestCount, `is`(1 + MAX_RETRIES))
        }

    @Test
    fun `GET should retry on timeout`() =
        runBlocking {
            clientRespondingWith { throw HttpRequestTimeoutException("https://example.com", 1) }.use { client ->
                runCatching { client.get("https://example.com") }
            }

            assertThat(requestCount, `is`(1 + MAX_RETRIES))
        }

    @Test
    fun `POST should not retry on network exception`() =
        runBlocking {
            clientRespondingWith { throw IOException("network down") }.use { client ->
                runCatching { client.post("https://example.com") }
            }

            assertThat(requestCount, `is`(1))
        }

    private companion object {
        const val MAX_RETRIES = 3
    }
}
