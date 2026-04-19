package com.yikwing.extension.util

import android.graphics.Color
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * ColorExtensions 单元测试
 *
 * `toColorInt()` 内部调用 `Color.parseColor(String)`，是 Android framework 的
 * native 方法；通过 mockkStatic 提供一个仅覆盖常用格式的伪实现来隔离依赖。
 */
class ColorExtensionsTest {
    @Before
    fun setup() {
        mockkStatic(Color::class)
        every { Color.parseColor(any()) } answers {
            val input = firstArg<String>()
            val hex = input.removePrefix("#")
            when (hex.length) {
                6 -> 0xFF_00_00_00.toInt() or hex.toLong(16).toInt()
                8 -> hex.toLong(16).toInt()
                else -> throw IllegalArgumentException("Unknown color: $input")
            }
        }
    }

    @After
    fun teardown() {
        unmockkStatic(Color::class)
    }

    @Test
    fun `alphaColor 100 percent should keep rgb with full alpha`() {
        assertThat("#FF0000".alphaColor(100), `is`(0xFFFF0000.toInt()))
    }

    @Test
    fun `alphaColor 0 percent should be fully transparent`() {
        assertThat("#FF0000".alphaColor(0), `is`(0x00FF0000))
    }

    @Test
    fun `alphaColor 50 percent should map to alpha 0x80`() {
        // (50 * 255 + 50) / 100 = 128 = 0x80
        assertThat("#FF0000".alphaColor(50), `is`(0x80FF0000.toInt()))
    }

    @Test
    fun `alphaColor should accept hex without hash prefix`() {
        assertThat("FF0000".alphaColor(50), `is`(0x80FF0000.toInt()))
    }

    @Test
    fun `alphaColor should clamp alpha above 100`() {
        assertThat("#FF0000".alphaColor(200), `is`(0xFFFF0000.toInt()))
    }

    @Test
    fun `alphaColor should clamp negative alpha`() {
        assertThat("#FF0000".alphaColor(-10), `is`(0x00FF0000))
    }

    @Test
    fun `alphaColor should strip existing alpha bits`() {
        // 输入已含 alpha=0x80，新 alpha=100% 应重置为 0xFF
        assertThat("#80FF0000".alphaColor(100), `is`(0xFFFF0000.toInt()))
    }
}