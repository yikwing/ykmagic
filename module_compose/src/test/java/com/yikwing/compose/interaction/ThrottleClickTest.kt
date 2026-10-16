package com.yikwing.compose.interaction

import android.os.SystemClock
import android.os.Trace
import android.util.Log
import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.ControlledComposition
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.disableHotReloadMode
import androidx.compose.runtime.mutableStateOf
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlin.coroutines.EmptyCoroutineContext
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.sameInstance
import org.junit.After
import org.junit.Before
import org.junit.Test

class ThrottleClickTest {
    private var fakeTime = 0L
    private lateinit var recomposer: Recomposer
    private lateinit var composition: ControlledComposition

    @Before
    fun setup() {
        mockkStatic(SystemClock::class, Trace::class, Log::class)
        every { SystemClock.elapsedRealtime() } answers { fakeTime }
        every { Trace.beginSection(any()) } just Runs
        every { Trace.endSection() } just Runs
        every { Log.e(any(), any<String>(), any()) } returns 0
        recomposer = Recomposer(EmptyCoroutineContext)
        composition = ControlledComposition(NoOpApplier(), recomposer)
    }

    @After
    fun teardown() {
        try {
            composition.dispose()
            recomposer.cancel()
        } finally {
            unmockkStatic(SystemClock::class, Trace::class, Log::class)
        }
    }

    // ==================== 时间边界 ====================

    @Test
    fun `should accept first click at zero uptime`() {
        var count = 0
        val click = createClick { count++ }

        click()

        assertThat(count, `is`(1))
    }

    @Test
    fun `should ignore repeated clicks within default interval`() {
        var count = 0
        val click = createClick { count++ }

        click()
        click()
        fakeTime = 499L
        click()

        assertThat(count, `is`(1))
    }

    @Test
    fun `should accept exact boundary without extending interval for ignored clicks`() {
        var count = 0
        val click = createClick { count++ }

        click()
        fakeTime = 499L
        click()
        fakeTime = 500L
        click()
        fakeTime = 999L
        click()
        fakeTime = 1_000L
        click()

        assertThat(count, `is`(3))
    }

    @Test
    fun `should accept every click with zero interval`() {
        var count = 0
        val click = createClick(intervalMs = 0L) { count++ }

        repeat(3) { click() }

        assertThat(count, `is`(3))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `should reject negative interval during composition`() {
        // Live Edit 会捕获组合异常，测试中关闭它以验证公开 API 的参数校验。
        disableHotReloadMode()
        createClick(intervalMs = -1L) {}
    }

    // ==================== 重组 ====================

    @Test
    fun `should use latest callback without changing click identity or resetting interval`() {
        val calls = mutableListOf<String>()
        val callback = mutableStateOf<() -> Unit>({ calls += "old" })
        lateinit var click: () -> Unit
        composition.setContent {
            click = rememberThrottleClick(onClick = callback.value)
        }
        val retainedClick = click
        retainedClick()

        callback.value = { calls += "new" }
        recompose(callback)
        assertThat(click, sameInstance(retainedClick))
        fakeTime = 100L
        retainedClick()
        fakeTime = 500L
        retainedClick()

        assertThat(calls, `is`(listOf("old", "new")))
    }

    @Test
    fun `should apply changed interval while keeping last accepted click time`() {
        var count = 0
        val interval = mutableStateOf(500L)
        lateinit var click: () -> Unit
        composition.setContent {
            click = rememberThrottleClick(intervalMs = interval.value) { count++ }
        }
        click()

        interval.value = 100L
        recompose(interval)
        fakeTime = 50L
        click()
        assertThat(count, `is`(1))
        fakeTime = 100L
        click()

        assertThat(count, `is`(2))
    }

    private fun createClick(intervalMs: Long = 500L, onClick: () -> Unit): () -> Unit {
        lateinit var click: () -> Unit
        composition.setContent {
            click = rememberThrottleClick(intervalMs, onClick)
        }
        return click
    }

    private fun recompose(changedState: Any) {
        composition.recordModificationsOf(setOf(changedState))
        assertThat(composition.recompose(), `is`(true))
        composition.applyChanges()
        composition.applyLateChanges()
        composition.changesApplied()
    }

    // 只测试 remember 和重组，不创建 UI 节点，也不需要真实帧或等待时间。
    private class NoOpApplier : AbstractApplier<Unit>(Unit) {
        override fun insertTopDown(index: Int, instance: Unit) = Unit
        override fun insertBottomUp(index: Int, instance: Unit) = Unit
        override fun remove(index: Int, count: Int) = Unit
        override fun move(from: Int, to: Int, count: Int) = Unit
        override fun onClear() = Unit
    }
}
