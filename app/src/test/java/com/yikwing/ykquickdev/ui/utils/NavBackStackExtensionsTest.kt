package com.yikwing.ykquickdev.ui.utils

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class NavBackStackExtensionsTest {
    private data class Route(val name: String) : NavKey

    private val a = Route("A")
    private val b = Route("B")
    private val c = Route("C")
    private val d = Route("D")

    @Test
    fun `inclusive replacement should replace the root and its descendants`() {
        val stack = NavBackStack<NavKey>(a, b, c)

        stack.replaceUpTo(d, upTo = a, inclusive = true)

        assertThat(stack.toList(), `is`(listOf<NavKey>(d)))
    }

    @Test
    fun `inclusive replacement should work with only a root`() {
        val stack = NavBackStack<NavKey>(a)

        stack.replaceUpTo(d, upTo = a, inclusive = true)

        assertThat(stack.toList(), `is`(listOf<NavKey>(d)))
    }

    @Test
    fun `exclusive replacement should retain its target`() {
        val stack = NavBackStack<NavKey>(a, b, c)

        stack.replaceUpTo(d, upTo = b)

        assertThat(stack.toList(), `is`(listOf<NavKey>(a, b, d)))
    }

    @Test
    fun `inclusive replacement should preserve entries below its target`() {
        val stack = NavBackStack<NavKey>(a, b, c)

        stack.replaceUpTo(d, upTo = b, inclusive = true)

        assertThat(stack.toList(), `is`(listOf<NavKey>(a, d)))
    }

    @Test
    fun `replacement should use the last matching target`() {
        val stack = NavBackStack<NavKey>(a, b, a, c)

        stack.replaceUpTo(d, upTo = a, inclusive = true)

        assertThat(stack.toList(), `is`(listOf<NavKey>(a, b, d)))
    }

    @Test
    fun `missing target should append the new route`() {
        val stack = NavBackStack<NavKey>(a, b)

        stack.replaceUpTo(d, upTo = c, inclusive = true)

        assertThat(stack.toList(), `is`(listOf<NavKey>(a, b, d)))
    }

    @Test
    fun `standalone inclusive pop should still protect the root`() {
        val stack = NavBackStack<NavKey>(a, b)

        assertThat(stack.popTo(a, inclusive = true), `is`(false))
        assertThat(stack.toList(), `is`(listOf<NavKey>(a, b)))
    }
}
