package com.yikwing.extension.util

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * InitState 单元测试
 *
 * 覆盖 sealed class 的两种状态、isInitialized 谓词，
 * 以及 getOrNull / getOrElse 在各状态下的行为，含 null 作为有效值的边界。
 */
class InitStateTest {
    // ==================== isInitialized ====================

    @Test
    fun `Uninitialized should not be initialized`() {
        val state: InitState<String> = InitState.Uninitialized
        assertThat(state.isInitialized, `is`(false))
    }

    @Test
    fun `Value should be initialized`() {
        val state: InitState<String> = InitState.Value("x")
        assertThat(state.isInitialized, `is`(true))
    }

    @Test
    fun `Value carrying null should still be initialized`() {
        // 关键边界：T 允许为 nullable，Value(null) 已初始化
        val state: InitState<String?> = InitState.Value(null)
        assertThat(state.isInitialized, `is`(true))
    }

    // ==================== getOrNull ====================

    @Test
    fun `getOrNull on Uninitialized should return null`() {
        val state: InitState<Int> = InitState.Uninitialized
        assertThat(state.getOrNull(), nullValue())
    }

    @Test
    fun `getOrNull on Value should return data`() {
        val state: InitState<Int> = InitState.Value(42)
        assertThat(state.getOrNull(), `is`(42))
    }

    // ==================== getOrElse ====================

    @Test
    fun `getOrElse on Uninitialized should invoke fallback`() {
        var invocations = 0
        val result =
            (InitState.Uninitialized as InitState<String>).getOrElse {
                invocations++
                "fallback"
            }
        assertThat(result, `is`("fallback"))
        assertThat(invocations, `is`(1))
    }

    @Test
    fun `getOrElse on Value should not invoke fallback`() {
        var invocations = 0
        val result =
            InitState.Value("real").getOrElse {
                invocations++
                "fallback"
            }
        assertThat(result, `is`("real"))
        assertThat(invocations, `is`(0))
    }

    // ==================== sealed identity ====================

    @Test
    fun `Uninitialized should be a singleton data object`() {
        val a: InitState<Int> = InitState.Uninitialized
        val b: InitState<String> = InitState.Uninitialized
        // 引用相等即可，避免无谓的泛型强转
        assertThat(a === b, `is`(true))
    }

    @Test
    fun `Value equality should follow data class semantics`() {
        assertThat(InitState.Value(1), `is`(InitState.Value(1)))
    }

    @Test
    fun `Value should be instance of InitState`() {
        assertThat(InitState.Value(1) as Any, instanceOf(InitState::class.java))
    }
}
