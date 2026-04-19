package com.yikwing.extension.coroutines

import android.os.SystemClock
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * FlowExtensions 单元测试
 *
 * throttleFirst 依赖 SystemClock.elapsedRealtime()，通过 mockkStatic 伪造时间轴
 * 以避免实际 delay 带来的不确定性。
 */
class FlowExtensionsTest {
    private var fakeTime = 0L

    @Before
    fun setup() {
        mockkStatic(SystemClock::class)
        every { SystemClock.elapsedRealtime() } answers { fakeTime }
        fakeTime = 1_000L
    }

    @After
    fun teardown() {
        unmockkStatic(SystemClock::class)
    }

    @Test
    fun `throttleFirst should emit first event`() =
        runBlocking {
            val result = flow { emit(1) }.throttleFirst(100).toList()
            assertThat(result, `is`(listOf(1)))
        }

    @Test
    fun `throttleFirst should drop events within threshold`() =
        runBlocking {
            val result =
                flow {
                    fakeTime = 1_000L
                    emit(1)
                    fakeTime = 1_050L
                    emit(2)
                    fakeTime = 1_099L
                    emit(3)
                }.throttleFirst(100).toList()
            assertThat(result, `is`(listOf(1)))
        }

    @Test
    fun `throttleFirst should pass events beyond threshold`() =
        runBlocking {
            val result =
                flow {
                    fakeTime = 1_000L
                    emit(1) // pass
                    fakeTime = 1_050L
                    emit(2) // drop
                    fakeTime = 1_100L
                    emit(3) // pass (delta = 100 >= threshold)
                    fakeTime = 1_150L
                    emit(4) // drop
                    fakeTime = 1_250L
                    emit(5) // pass
                }.throttleFirst(100).toList()
            assertThat(result, `is`(listOf(1, 3, 5)))
        }

    @Test(expected = IllegalArgumentException::class)
    fun `throttleFirst should throw on zero threshold`() =
        runBlocking {
            flow<Int> {}.throttleFirst(0).toList()
            Unit
        }

    @Test(expected = IllegalArgumentException::class)
    fun `throttleFirst should throw on negative threshold`() =
        runBlocking {
            flow<Int> {}.throttleFirst(-1).toList()
            Unit
        }
}