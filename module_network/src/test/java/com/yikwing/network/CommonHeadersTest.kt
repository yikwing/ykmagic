package com.yikwing.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * CommonHeaders 插件单元测试
 *
 * 覆盖：公共头注入、强制覆盖请求级同名头、headers provider 每次请求重新求值。
 */
class CommonHeadersTest {
    private val sentHeaders = mutableListOf<Headers>()

    private fun clientWithHeaders(provider: () -> Map<String, String>): HttpClient =
        HttpClient(
            MockEngine { request ->
                sentHeaders += request.headers
                respondOk()
            },
        ) {
            install(CommonHeaders) { headers = provider }
        }

    @Test
    fun `should add common headers to request`() =
        runBlocking {
            clientWithHeaders { mapOf("version" to "1.0", HttpHeaders.UserAgent to "app_1.0") }
                .use { it.get("https://example.com") }

            assertThat(sentHeaders.single()["version"], `is`("1.0"))
            assertThat(sentHeaders.single()[HttpHeaders.UserAgent], `is`("app_1.0"))
        }

    @Test
    fun `should override header set on request`() =
        runBlocking {
            clientWithHeaders { mapOf("version" to "1.0") }
                .use { it.get("https://example.com") { header("version", "custom") } }

            assertThat(sentHeaders.single().getAll("version"), `is`(listOf("1.0")))
        }

    @Test
    fun `should evaluate provider on every request`() =
        runBlocking {
            var token = "a"
            clientWithHeaders { mapOf("token" to token) }.use { client ->
                client.get("https://example.com")
                token = "b"
                client.get("https://example.com")
            }

            assertThat(sentHeaders.map { it["token"] }, `is`(listOf("a", "b")))
        }
}
