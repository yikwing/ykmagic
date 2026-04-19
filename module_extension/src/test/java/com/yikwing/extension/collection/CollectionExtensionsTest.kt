package com.yikwing.extension.collection

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * CollectionExtensions 单元测试
 *
 * 覆盖 List/Array.lastOf 的正负索引分支、越界、空集合，以及
 * filterNullValue / filterValue 的 null 与等值替换语义。
 */
class CollectionExtensionsTest {
    // ==================== List.lastOf ====================

    @Test
    fun `List lastOf -1 should return last element`() {
        assertThat(listOf(1, 2, 3).lastOf(-1), `is`(3))
    }

    @Test
    fun `List lastOf -size should return first element`() {
        assertThat(listOf(1, 2, 3).lastOf(-3), `is`(1))
    }

    @Test
    fun `List lastOf out of range should return null`() {
        assertThat(listOf(1, 2, 3).lastOf(-4), nullValue())
    }

    @Test
    fun `List lastOf on empty should return null`() {
        assertThat(emptyList<Int>().lastOf(-1), nullValue())
    }

    @Test
    fun `List lastOf with non-negative argument should return null`() {
        // 契约：仅支持负索引，正数或 0 视为非法并返回 null
        assertThat(listOf(1, 2, 3).lastOf(0), nullValue())
        assertThat(listOf(1, 2, 3).lastOf(1), nullValue())
    }

    // ==================== Array.lastOf ====================

    @Test
    fun `Array lastOf -1 should return last element`() {
        assertThat(arrayOf("a", "b", "c").lastOf(-1), `is`("c"))
    }

    @Test
    fun `Array lastOf -size should return first element`() {
        assertThat(arrayOf("a", "b", "c").lastOf(-3), `is`("a"))
    }

    @Test
    fun `Array lastOf out of range should return null`() {
        assertThat(arrayOf("a", "b").lastOf(-5), nullValue())
    }

    @Test
    fun `Array lastOf on empty should return null`() {
        assertThat(emptyArray<String>().lastOf(-1), nullValue())
    }

    @Test
    fun `Array lastOf with non-negative argument should return null`() {
        assertThat(arrayOf(1, 2).lastOf(0), nullValue())
        assertThat(arrayOf(1, 2).lastOf(2), nullValue())
    }

    // ==================== filterNullValue ====================

    @Test
    fun `filterNullValue should return default when receiver is null`() {
        val value: String? = null
        assertThat(value.filterNullValue("default"), `is`("default"))
    }

    @Test
    fun `filterNullValue should return receiver when not null`() {
        val value: String? = "real"
        assertThat(value.filterNullValue("default"), `is`("real"))
    }

    // ==================== filterValue ====================

    @Test
    fun `filterValue should return default when receiver equals filterValue`() {
        val value: Int? = -1
        assertThat(value.filterValue(filterValue = -1, defaultValue = 0), `is`(0))
    }

    @Test
    fun `filterValue should return default when receiver is null`() {
        val value: Int? = null
        assertThat(value.filterValue(filterValue = -1, defaultValue = 0), `is`(0))
    }

    @Test
    fun `filterValue should return receiver when not matching filterValue`() {
        val value: Int? = 42
        assertThat(value.filterValue(filterValue = -1, defaultValue = 0), `is`(42))
    }

    @Test
    fun `filterValue with null filterValue should fall back when receiver is null`() {
        // this == filterValue 分支：两者都为 null 时相等，返回默认值
        val value: Int? = null
        assertThat(value.filterValue(filterValue = null, defaultValue = 99), `is`(99))
    }
}
