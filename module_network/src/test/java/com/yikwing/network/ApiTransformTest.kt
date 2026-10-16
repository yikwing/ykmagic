package com.yikwing.network

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
 * `ApiConfig.errorCodeChecker` 与 `ApiConfig.logger` 是进程级 @Volatile 单例,
 * 每个用例后恢复默认避免串扰。
 */
class ApiTransformTest {
    private val defaultChecker = ApiConfig.errorCodeChecker
    private val defaultLogger = ApiConfig.logger

    @Before
    fun setup() {
        // 默认 logger 走 android.util.Log,JVM 单测中未 mock 会抛异常,这里替换为空实现
        ApiConfig.logger = { _, _, _ -> }
    }

    @After
    fun teardown() {
        ApiConfig.errorCodeChecker = defaultChecker
        ApiConfig.logger = defaultLogger
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

    // ==================== toApiException ====================

    @Test
    fun `toApiException should keep original ApiException instance`() {
        val original = ApiException(code = 11, message = "biz")
        assertThat(original.toApiException(), sameInstance(original))
    }

    @Test
    fun `toApiException should wrap other throwable with default code`() {
        val cause = IOException("io")
        val wrapped = cause.toApiException()

        assertThat(wrapped.code, `is`(DEFAULT_ERROR_CODE))
        assertThat(wrapped.message, `is`("io"))
        assertThat(wrapped.cause, sameInstance<Throwable>(cause))
    }

    // ==================== requestStateFlow ====================

    // Loading 由最下游的 onStart 发出,因此在 Success 与 Error 路径上都必须是第一个元素。

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

    @Test
    fun `requestStateFlow should emit Error with ApiException for failure code`() =
        runBlocking {
            val states =
                requestStateFlow { BaseHttpResult(data = "x", errorMsg = "boom", errorCode = 7) }
                    .toList()

            assertThat(states.size, `is`(2))
            assertThat(states[0], `is`(RequestState.Loading))
            val error = states[1] as RequestState.Error
            assertThat(error.throwable.code, `is`(7))
            assertThat(error.throwable.message, `is`("boom"))
        }

    @Test
    fun `requestStateFlow should forward thrown ApiException unchanged`() =
        runBlocking {
            val original = ApiException(code = 99, message = "api-layer")
            val states = requestStateFlow<String> { throw original }.toList()

            assertThat(states.size, `is`(2))
            assertThat(states[0], `is`(RequestState.Loading))
            assertThat((states[1] as RequestState.Error).throwable, sameInstance(original))
        }

    @Test
    fun `requestStateFlow should wrap non-ApiException into default ApiException`() =
        runBlocking {
            val cause = IOException("network")
            val states = requestStateFlow<String> { throw cause }.toList()

            assertThat(states.size, `is`(2))
            assertThat(states[0], `is`(RequestState.Loading))
            val error = states[1] as RequestState.Error
            assertThat(error.throwable.code, `is`(DEFAULT_ERROR_CODE))
            assertThat(error.throwable.message, `is`("network"))
            // 注: coroutines stack-trace recovery 可能 clone IOException,所以用类型+message,不用 sameInstance
            assertThat(error.throwable.cause, instanceOf(IOException::class.java))
            assertThat(error.throwable.cause?.message, `is`("network"))
        }

    @Test
    fun `requestStateFlow should rethrow CancellationException`() =
        runBlocking {
            val marker = CancellationException("user-cancel")
            val caught =
                try {
                    requestStateFlow<Int> { throw marker }.toList()
                    null
                } catch (e: CancellationException) {
                    e
                }

            // 必须重抛到收集方,否则 RequestState.Error 会吞掉协程取消信号
            assertThat(caught, notNullValue())
            assertThat(caught!!.message, `is`("user-cancel"))
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

    // ==================== requestRawResult ====================

    @Test
    fun `requestRawResult should return success with raw value`() =
        runBlocking {
            val result = requestRawResult { "payload" }

            assertThat(result.getOrNull(), `is`("payload"))
        }

    @Test
    fun `requestRawResult should forward thrown ApiException unchanged`() =
        runBlocking {
            val original = ApiException(code = 77, message = "biz")
            val result = requestRawResult<Int> { throw original }

            assertThat(result.exceptionOrNull(), sameInstance<Throwable>(original))
        }

    @Test
    fun `requestRawResult should wrap non-ApiException into default ApiException`() =
        runBlocking {
            val cause = IOException("io")
            val result = requestRawResult<Int> { throw cause }

            val wrapped = result.exceptionOrNull() as ApiException
            assertThat(wrapped.code, `is`(DEFAULT_ERROR_CODE))
            assertThat(wrapped.cause, sameInstance<Throwable>(cause))
        }

    @Test
    fun `requestRawResult should rethrow CancellationException`() =
        runBlocking {
            val marker = CancellationException("user-cancel")
            val caught =
                try {
                    requestRawResult<Int> { throw marker }
                    null
                } catch (e: CancellationException) {
                    e
                }

            assertThat(caught, notNullValue())
            assertThat(caught!!.message, `is`("user-cancel"))
        }
}
