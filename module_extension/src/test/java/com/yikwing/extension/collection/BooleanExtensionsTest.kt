package com.yikwing.extension.collection

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class BooleanExtensionsTest {
    // ==================== yes / no ====================

    @Test
    fun `yes should execute block when true`() {
        var called = false
        true.yes { called = true }
        assertThat(called, `is`(true))
    }

    @Test
    fun `yes should not execute block when false`() {
        var called = false
        false.yes { called = true }
        assertThat(called, `is`(false))
    }

    @Test
    fun `no should execute block when false`() {
        var called = false
        false.no { called = true }
        assertThat(called, `is`(true))
    }

    @Test
    fun `yes and no should be chainable and return receiver`() {
        val log = mutableListOf<String>()
        val result =
            true
                .yes { log += "yes" }
                .no { log += "no" }
        assertThat(result, `is`(true))
        assertThat(log, `is`(listOf("yes")))
    }

    // ==================== choose ====================

    @Test
    fun `choose should return whenTrue for true`() {
        assertThat(true.choose("a", "b"), `is`("a"))
    }

    @Test
    fun `choose should return whenFalse for false`() {
        assertThat(false.choose("a", "b"), `is`("b"))
    }

    // ==================== chooseLazy ====================

    @Test
    fun `chooseLazy should only invoke chosen branch`() {
        var trueCalls = 0
        var falseCalls = 0
        val result =
            true.chooseLazy(
                whenTrue = {
                    trueCalls++
                    "a"
                },
                whenFalse = {
                    falseCalls++
                    "b"
                },
            )
        assertThat(result, `is`("a"))
        assertThat(trueCalls, `is`(1))
        assertThat(falseCalls, `is`(0))
    }

    @Test
    fun `chooseLazy should pick false branch when false`() {
        var trueCalls = 0
        var falseCalls = 0
        val result =
            false.chooseLazy(
                whenTrue = {
                    trueCalls++
                    "a"
                },
                whenFalse = {
                    falseCalls++
                    "b"
                },
            )
        assertThat(result, `is`("b"))
        assertThat(trueCalls, `is`(0))
        assertThat(falseCalls, `is`(1))
    }

    // ==================== thenValue / thenRun ====================

    @Test
    fun `thenValue should return value when true`() {
        assertThat(true.thenValue("a"), `is`("a"))
    }

    @Test
    fun `thenValue should return null when false`() {
        assertThat(false.thenValue("a"), nullValue())
    }

    @Test
    fun `thenRun should invoke block and return result when true`() {
        var called = 0
        val result =
            true.thenRun {
                called++
                "a"
            }
        assertThat(result, `is`("a"))
        assertThat(called, `is`(1))
    }

    @Test
    fun `thenRun should return null and skip block when false`() {
        var called = 0
        val result =
            false.thenRun {
                called++
                "a"
            }
        assertThat(result, nullValue())
        assertThat(called, `is`(0))
    }

    // ==================== toInt / toBoolean ====================

    @Test
    fun `Boolean toInt should map true to 1 and false to 0`() {
        assertThat(true.toInt(), `is`(1))
        assertThat(false.toInt(), `is`(0))
    }

    @Test
    fun `Int toBoolean should map 1 to true and 0 to false`() {
        assertThat(1.toBoolean(), `is`(true))
        assertThat(0.toBoolean(), `is`(false))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Int toBoolean should throw for non-binary value`() {
        2.toBoolean()
    }
}