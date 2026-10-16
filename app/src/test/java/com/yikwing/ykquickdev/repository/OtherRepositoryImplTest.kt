package com.yikwing.ykquickdev.repository

import com.yikwing.network.ApiConfig
import com.yikwing.network.ApiException
import com.yikwing.ykquickdev.api.apiserver.HttpBinApi
import com.yikwing.ykquickdev.api.entity.Headers
import com.yikwing.ykquickdev.api.entity.HttpBinHeaders
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.IOException

/**
 * OtherRepositoryImpl 单元测试
 *
 * 覆盖 initHttpBinData 的成功解包与异常包装路径。
 * CancellationException 重抛由 requestRawResult 自身测试覆盖。
 *
 * `ApiConfig.logger` 是进程级单例,默认走 android.util.Log,每个用例后恢复默认。
 */
class OtherRepositoryImplTest {
    private val defaultLogger = ApiConfig.logger
    private val httpBinApi = mockk<HttpBinApi>()
    private val repository = OtherRepositoryImpl(httpBinApi = httpBinApi)

    @Before
    fun setup() {
        ApiConfig.logger = { _, _, _ -> }
    }

    @After
    fun teardown() {
        ApiConfig.logger = defaultLogger
    }

    @Test
    fun `initHttpBinData should return headers when request succeeds`() =
        runBlocking {
            val headers = Headers(host = "httpbin.org", userAgent = "ktor", traceId = "t-1")
            coEvery { httpBinApi.getHeaders(any()) } returns HttpBinHeaders(headers)

            val result = repository.initHttpBinData()

            assertThat(result.getOrNull(), `is`(headers))
        }

    @Test
    fun `initHttpBinData should return ApiException when request throws`() =
        runBlocking {
            coEvery { httpBinApi.getHeaders(any()) } throws IOException("network down")

            val result = repository.initHttpBinData()

            assertThat(result.exceptionOrNull(), instanceOf(ApiException::class.java))
            assertThat(result.exceptionOrNull()!!.message, `is`("network down"))
        }
}
