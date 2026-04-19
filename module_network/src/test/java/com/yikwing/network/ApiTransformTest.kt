package com.yikwing.network

import android.util.Log
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

/**
 * ApiTransform 单元测试
 *
 * 覆盖 transformHttpResult 错误码分支,以及 requestStateFlow / requestResult 的
 * Success / Error / 异常包装 / CancellationException 重抛路径。
 *
 * `ApiConfig.errorCodeChecker` 是进程级 @Volatile 单例,每个用例后恢复默认避免串扰。
 */
class ApiTransformTest {
    private val defaultChecker = ApiConfig.errorCodeChecker

    @Before
    fun setup() {
        // requestStateFlow / requestResult 异常分支会走 Log.e
        mockkStatic(Log::class)
        every { Log.e(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>(), any()) } returns 0
    }

    @After
    fun teardown() {
        ApiConfig.errorCodeChecker = defaultChecker
        unmockkStatic(Log::class)
    }

    // ==================== transformHttpResult ====================

    @Test
    fun `transformHttpResult should return data when code is success`() {
        val result = BaseHttpResult(data = "payload", errorMsg = "", errorCode = 0)
        assertThat(transformHttpResult(result), `is`("payload"))
    }

    @Test
    fun `transformHttpResult should return null data when code is success`() {
        val result = BaseHttpResult<String>(data = null, errorMsg = "", errorCode = 0)
        assertThat(transformHttpResult(result), nullValue())
    }

    @Test
    fun `transformHttpResult should throw ApiException when code fails`() {
        val result = BaseHttpResult(data = "ignored", errorMsg = "bad", errorCode = 42)
        val thrown =
            try {
                transformHttpResult(result)
                null
            } catch (e: ApiException) {
                e
            }

        assertThat(thrown, notNullValue())
        assertThat(thrown!!.code, `is`(42))
        assertThat(thrown.message, `is`("bad"))
    }

    @Test
    fun `transformHttpResult should respect custom errorCodeChecker`() {
        ApiConfig.errorCodeChecker = { it != 200 }
        val ok = BaseHttpResult(data = 1, errorMsg = "", errorCode = 200)
        val bad = BaseHttpResult(data = 1, errorMsg = "oh", errorCode = 0)

        assertThat(transformHttpResult(ok), `is`(1))
        val thrown =
            try {
                transformHttpResult(bad)
                null
            } catch (e: ApiException) {
                e
            }
        assertThat(thrown?.code, `is`(0))
    }

    // ==================== requestStateFlow ====================

    @Test
    fun `requestStateFlow should emit Loading then Success`() =
        runBlocking {
            val states =
                requestStateFlow { BaseHttpResult(data = "ok", errorMsg = "", errorCode = 0) }
                    .toList()

            assertThat(states.size, `is`(2))
            assertThat(states[0], `is`(RequestState.Loading))
            assertThat(states[1], instanceOf(RequestState.Success::class.java))
            assertThat((states[1] as RequestState.Success).value, `is`("ok"))
        }

    // 说明: 下面三个 Error 路径不断言 states[0] == Loading,因为 `.flowOn(Dispatchers.IO)` 的
    // ChannelFlow 在 producer 抛异常时可能丢弃 buffered 元素(Kotlin 协程已知行为)。
    // Loading 的存在性已由上面的 Success 测试覆盖。

    @Test
    fun `requestStateFlow should emit Error with ApiException for failure code`() =
        runBlocking {
            val states =
                requestStateFlow { BaseHttpResult(data = "x", errorMsg = "boom", errorCode = 7) }
                    .toList()

            val error = states.filterIsInstance<RequestState.Error>().single()
            assertThat(error.throwable.code, `is`(7))
            assertThat(error.throwable.message, `is`("boom"))
        }

    @Test
    fun `requestStateFlow should forward thrown ApiException unchanged`() =
        runBlocking {
            val original = ApiException(code = 99, message = "api-layer")
            val states =
                requestStateFlow<String> { throw original }.toList()

            val error = states.filterIsInstance<RequestState.Error>().single()
            assertThat(error.throwable, sameInstance(original))
        }

    @Test
    fun `requestStateFlow should wrap non-ApiException into default ApiException`() =
        runBlocking {
            val cause = IOException("network")
            val states =
                requestStateFlow<String> { throw cause }.toList()

            val error = states.filterIsInstance<RequestState.Error>().single()
            assertThat(error.throwable.code, `is`(DEFAULT_ERROR_CODE))
            assertThat(error.throwable.message, `is`("network"))
            // 注: coroutines stack-trace recovery 可能 clone IOException,所以用类型+message,不用 sameInstance
            assertThat(error.throwable.cause, instanceOf(IOException::class.java))
            assertThat(error.throwable.cause?.message, `is`("network"))
        }

    // ==================== requestResult ====================

    @Test
    fun `requestResult should return success result when code is success`() =
        runBlocking {
            val result = requestResult { BaseHttpResult(data = 123, errorMsg = "", errorCode = 0) }

            assertThat(result.isSuccess, `is`(true))
            assertThat(result.getOrNull(), `is`(123))
        }

    @Test
    fun `requestResult should return failure with ApiException when code fails`() =
        runBlocking {
            val result =
                requestResult { BaseHttpResult(data = 0, errorMsg = "nope", errorCode = 5) }

            assertThat(result.isFailure, `is`(true))
            val exception = result.exceptionOrNull() as ApiException
            assertThat(exception.code, `is`(5))
            assertThat(exception.message, `is`("nope"))
        }

    @Test
    fun `requestResult should forward thrown ApiException unchanged`() =
        runBlocking {
            val original = ApiException(code = 77, message = "biz")
            val result = requestResult<Int> { throw original }

            assertThat(result.exceptionOrNull(), sameInstance<Throwable>(original))
        }

    @Test
    fun `requestResult should wrap non-ApiException into default ApiException`() =
        runBlocking {
            val cause = IOException("io")
            val result = requestResult<Int> { throw cause }

            val wrapped = result.exceptionOrNull() as ApiException
            assertThat(wrapped.code, `is`(DEFAULT_ERROR_CODE))
            assertThat(wrapped.cause, sameInstance<Throwable>(cause))
        }

    @Test
    fun `requestResult should rethrow CancellationException`() =
        runBlocking {
            val marker = CancellationException("user-cancel")
            val caught =
                try {
                    requestResult<Int> { throw marker }
                    null
                } catch (e: CancellationException) {
                    e
                }

            // 必须重抛到调用方,否则 Result.failure 会吞掉协程取消信号
            assertThat(caught, notNullValue())
            assertThat(caught!!.message, `is`("user-cancel"))
        }
}
