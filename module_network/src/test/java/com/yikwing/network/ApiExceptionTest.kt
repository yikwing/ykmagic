package com.yikwing.network

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import java.io.IOException

/**
 * ApiException 单元测试
 *
 * 确保 createDefault 走 DEFAULT_ERROR_CODE、message/cause 按原样透传,
 * 因为上层 requestStateFlow/requestResult 的降级路径依赖这两个字段。
 */
class ApiExceptionTest {
    // ==================== 普通构造 ====================

    @Test
    fun `constructor should keep code message and cause`() {
        val cause = IOException("boom")
        val exception = ApiException(code = 500, message = "server error", cause = cause)

        assertThat(exception.code, `is`(500))
        assertThat(exception.message, `is`("server error"))
        assertThat(exception.cause, sameInstance<Throwable>(cause))
    }

    @Test
    fun `constructor with default cause should be null`() {
        val exception = ApiException(code = 400, message = "bad request")

        assertThat(exception.cause, nullValue())
    }

    // ==================== createDefault ====================

    @Test
    fun `createDefault should use DEFAULT_ERROR_CODE`() {
        val exception = ApiException.createDefault(message = "unknown")

        assertThat(exception.code, `is`(DEFAULT_ERROR_CODE))
        assertThat(DEFAULT_ERROR_CODE, `is`(-1))
        assertThat(exception.message, `is`("unknown"))
    }

    @Test
    fun `createDefault should propagate cause when provided`() {
        val cause = IllegalStateException("root")
        val exception = ApiException.createDefault(message = "wrap", cause = cause)

        assertThat(exception.cause, sameInstance<Throwable>(cause))
    }

    @Test
    fun `createDefault should accept null message`() {
        val exception = ApiException.createDefault(message = null)

        assertThat(exception.message, nullValue())
        assertThat(exception.code, `is`(DEFAULT_ERROR_CODE))
    }
}
