package com.yikwing.network

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * RequestState 单元测试
 *
 * 覆盖三态的取值/判定/回调/DSL 四组 API。collectState 用 flowOf + runBlocking
 * 直接观察分派顺序,与 FlowExtensionsTest 的 Flow 测试套路一致。
 */
class RequestStateTest {
    private val error = ApiException(code = 1, message = "oops")

    // ==================== onSuccess ====================

    @Test
    fun `onSuccess should invoke callback on Success`() {
        var captured: Int? = null
        val state: RequestState<Int> = RequestState.Success(42)
        val returned = state.onSuccess { captured = it }

        assertThat(captured, `is`(42))
        assertThat(returned, sameInstance(state))
    }

    @Test
    fun `onSuccess should not invoke on Loading or Error`() {
        var invoked = 0
        (RequestState.Loading as RequestState<Int>).onSuccess { invoked++ }
        (RequestState.Error(error) as RequestState<Int>).onSuccess { invoked++ }

        assertThat(invoked, `is`(0))
    }

    // ==================== onFailure ====================

    @Test
    fun `onFailure should invoke callback on Error`() {
        var captured: ApiException? = null
        val state: RequestState<Int> = RequestState.Error(error)
        val returned = state.onFailure { captured = it }

        assertThat(captured, sameInstance(error))
        assertThat(returned, sameInstance(state))
    }

    @Test
    fun `onFailure should not invoke on Loading or Success`() {
        var invoked = 0
        (RequestState.Loading as RequestState<Int>).onFailure { invoked++ }
        RequestState.Success(1).onFailure { invoked++ }

        assertThat(invoked, `is`(0))
    }

    // ==================== getOrNull / getOrDefault ====================

    @Test
    fun `getOrNull should unwrap Success and null out Loading Error`() {
        assertThat(RequestState.Success("v").getOrNull(), `is`("v"))
        assertThat((RequestState.Loading as RequestState<String>).getOrNull(), nullValue())
        assertThat((RequestState.Error(error) as RequestState<String>).getOrNull(), nullValue())
    }

    @Test
    fun `getOrDefault should fall back for non-Success`() {
        assertThat(RequestState.Success("v").getOrDefault("d"), `is`("v"))
        assertThat((RequestState.Loading as RequestState<String>).getOrDefault("d"), `is`("d"))
        assertThat((RequestState.Error(error) as RequestState<String>).getOrDefault("d"), `is`("d"))
    }

    // ==================== getOrElse ====================

    @Test
    fun `getOrElse should pass null to callback on Loading`() {
        var receivedException: ApiException? = error // 非 null 初值,确认被覆盖为 null
        var invoked = false
        val result =
            (RequestState.Loading as RequestState<String>).getOrElse {
                invoked = true
                receivedException = it
                "fallback"
            }

        assertThat(result, `is`("fallback"))
        assertThat(invoked, `is`(true))
        assertThat(receivedException, nullValue())
    }

    @Test
    fun `getOrElse should pass exception to callback on Error`() {
        var received: ApiException? = null
        val result =
            (RequestState.Error(error) as RequestState<String>).getOrElse {
                received = it
                "fallback"
            }

        assertThat(result, `is`("fallback"))
        assertThat(received, sameInstance(error))
    }

    @Test
    fun `getOrElse should not invoke callback on Success`() {
        var invoked = false
        val result =
            RequestState.Success("v").getOrElse {
                invoked = true
                "fallback"
            }

        assertThat(result, `is`("v"))
        assertThat(invoked, `is`(false))
    }

    // ==================== exceptionOrNull ====================

    @Test
    fun `exceptionOrNull should return exception only on Error`() {
        assertThat((RequestState.Error(error) as RequestState<Int>).exceptionOrNull(), sameInstance(error))
        assertThat(RequestState.Success(1).exceptionOrNull(), nullValue())
        assertThat((RequestState.Loading as RequestState<Int>).exceptionOrNull(), nullValue())
    }

    // ==================== isLoading / isSuccess / isFailure ====================

    @Test
    fun `isLoading should be true only for Loading`() {
        assertThat((RequestState.Loading as RequestState<Int>).isLoading, `is`(true))
        assertThat(RequestState.Success(1).isLoading, `is`(false))
        assertThat((RequestState.Error(error) as RequestState<Int>).isLoading, `is`(false))
    }

    @Test
    fun `isSuccess should be true only for Success`() {
        assertThat(RequestState.Success(1).isSuccess, `is`(true))
        assertThat((RequestState.Loading as RequestState<Int>).isSuccess, `is`(false))
        assertThat((RequestState.Error(error) as RequestState<Int>).isSuccess, `is`(false))
    }

    @Test
    fun `isFailure should be true only for Error`() {
        assertThat((RequestState.Error(error) as RequestState<Int>).isFailure, `is`(true))
        assertThat(RequestState.Success(1).isFailure, `is`(false))
        assertThat((RequestState.Loading as RequestState<Int>).isFailure, `is`(false))
    }

    // ==================== Null value carrying ====================

    @Test
    fun `Success can carry null value`() {
        val state: RequestState<String?> = RequestState.Success(null)
        assertThat(state.isSuccess, `is`(true))
        assertThat(state.getOrNull(), nullValue())
    }

    // ==================== collectState DSL ====================

    @Test
    fun `collectState should dispatch Loading Success Error in order`() =
        runBlocking {
            val events = mutableListOf<String>()
            val flow =
                flowOf<RequestState<Int>>(
                    RequestState.Loading,
                    RequestState.Success(7),
                    RequestState.Error(error),
                )

            flow.collectState {
                onLoading { events.add("loading") }
                onSuccess { events.add("success=$it") }
                onFailure { events.add("failure=${it.message}") }
            }

            assertThat(events, `is`(listOf("loading", "success=7", "failure=oops")))
        }

    @Test
    fun `collectState with default builder should not crash when callbacks missing`() =
        runBlocking {
            val flow =
                flowOf<RequestState<Int>>(
                    RequestState.Loading,
                    RequestState.Success(1),
                    RequestState.Error(error),
                )
            flow.collectState {
                // 故意不配置任何回调,走默认 no-op
            }
        }

    @Test
    fun `collectState last onSuccess registration wins`() =
        runBlocking {
            val events = mutableListOf<String>()
            flowOf<RequestState<Int>>(RequestState.Success(1)).collectState {
                onSuccess { events.add("first=$it") }
                onSuccess { events.add("second=$it") }
            }

            assertThat(events, `is`(listOf("second=1")))
        }
}
