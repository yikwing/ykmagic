package com.yikwing.extension.date

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * LocalDateUtils 单元测试
 *
 * 对纯格式化 / 解析 / 计算类函数使用固定 fixture 锁定行为；对依赖"当前时间"
 * 的谓词用 LocalDate.now() 派生出"昨天 / 明天"作为基准，避免在 JDK17+ 上
 * 通过 mockkStatic 拦截 java.time 引发的 IllegalAccessException
 * （java.base 模块默认不对反射开放）。
 */
class LocalDateUtilsTest {
    // ==================== LocalDateTime ====================

    @Test
    fun `formatDateTime should produce standard layout`() {
        val dt = LocalDateTime.of(2024, 12, 10, 14, 30, 5)
        assertThat(dt.formatDateTime(), `is`("2024-12-10 14:30:05"))
    }

    @Test
    fun `LocalDateTime format with custom pattern`() {
        val dt = LocalDateTime.of(2024, 12, 10, 14, 30, 0)
        assertThat(dt.format("yyyy/MM/dd HH:mm"), `is`("2024/12/10 14:30"))
    }

    // ==================== LocalDate ====================

    @Test
    fun `formatDate should produce yyyy-MM-dd`() {
        val date = LocalDate.of(2024, 1, 5)
        assertThat(date.formatDate(), `is`("2024-01-05"))
    }

    @Test
    fun `LocalDate format with custom pattern`() {
        val date = LocalDate.of(2024, 1, 5)
        assertThat(date.format("yyyy/MM/dd"), `is`("2024/01/05"))
    }

    @Test
    fun `toLocalDate should parse default yyyy-MM-dd`() {
        assertThat("2024-12-10".toLocalDate(), `is`(LocalDate.of(2024, 12, 10)))
    }

    @Test
    fun `toLocalDate with custom pattern`() {
        assertThat(
            "2024/12/10".toLocalDate("yyyy/MM/dd"),
            `is`(LocalDate.of(2024, 12, 10)),
        )
    }

    @Test(expected = java.time.format.DateTimeParseException::class)
    fun `toLocalDate should throw on bad input`() {
        "not-a-date".toLocalDate()
    }

    @Test
    fun `daysBetween should return positive diff`() {
        val start = LocalDate.of(2024, 1, 1)
        val end = LocalDate.of(2024, 12, 31)
        assertThat(start.daysBetween(end), `is`(365L)) // 2024 为闰年
    }

    @Test
    fun `daysBetween should return negative when end is before start`() {
        val start = LocalDate.of(2024, 1, 10)
        val end = LocalDate.of(2024, 1, 1)
        assertThat(start.daysBetween(end), `is`(-9L))
    }

    @Test
    fun `daysBetween same date should be zero`() {
        val d = LocalDate.of(2024, 5, 5)
        assertThat(d.daysBetween(d), `is`(0L))
    }

    // ==================== isToday / isPast / isFuture ====================

    @Test
    fun `isToday should be true for today`() {
        assertThat(LocalDate.now().isToday(), `is`(true))
    }

    @Test
    fun `isToday should be false for yesterday`() {
        assertThat(LocalDate.now().minusDays(1).isToday(), `is`(false))
    }

    @Test
    fun `isPast should be true for yesterday`() {
        assertThat(LocalDate.now().minusDays(1).isPast(), `is`(true))
    }

    @Test
    fun `isPast should be false for today`() {
        // 边界：today 本身不算过去
        assertThat(LocalDate.now().isPast(), `is`(false))
    }

    @Test
    fun `isFuture should be true for tomorrow`() {
        assertThat(LocalDate.now().plusDays(1).isFuture(), `is`(true))
    }

    @Test
    fun `isFuture should be false for today`() {
        // 边界：today 本身不算未来
        assertThat(LocalDate.now().isFuture(), `is`(false))
    }

    // ==================== LocalTime ====================

    @Test
    fun `formatTime should produce HH-mm-ss`() {
        val t = LocalTime.of(9, 5, 3)
        assertThat(t.formatTime(), `is`("09:05:03"))
    }

    @Test
    fun `LocalTime format with custom pattern`() {
        val t = LocalTime.of(9, 5, 3)
        assertThat(t.format("HH:mm"), `is`("09:05"))
    }

    // ==================== now* ====================

    @Test
    fun `nowDate should equal today formatted`() {
        assertThat(nowDate(), `is`(LocalDate.now().formatDate()))
    }
}
