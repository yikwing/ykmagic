package com.yikwing.extension.delegate

import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import kotlin.properties.Delegates

/**
 * NotNullSingleVar 单元测试
 *
 * 语义：仅允许一次性写入，读取前必须先 set，否则抛 IllegalStateException；
 * 二次 set 也抛异常，防止全局单例被静默覆盖。
 */
class NotNullSingleVarTest {
    // 每个测试使用独立的宿主以规避单次写入约束的跨用例污染
    private class Host {
        var prop: String by Delegates.notNullSingle()
    }

    @Test
    fun `read after set should return the set value`() {
        val host = Host()
        host.prop = "hello"
        assertThat(host.prop, `is`("hello"))
    }

    @Test
    fun `read before set should throw IllegalStateException`() {
        val host = Host()
        val ex =
            try {
                host.prop
                null
            } catch (e: IllegalStateException) {
                e
            }
        assertThat(ex != null, `is`(true))
        // 错误消息里应包含属性名，便于排查现场
        assertThat(ex!!.message ?: "", containsString("prop"))
        assertThat(ex.message ?: "", containsString("initialized before get"))
    }

    @Test
    fun `second set should throw IllegalStateException`() {
        val host = Host()
        host.prop = "first"
        val ex =
            try {
                host.prop = "second"
                null
            } catch (e: IllegalStateException) {
                e
            }
        assertThat(ex != null, `is`(true))
        assertThat(ex!!.message ?: "", containsString("already initialized"))
    }

    @Test
    fun `different host instances should have independent slots`() {
        val a = Host()
        val b = Host()
        a.prop = "a"
        b.prop = "b"
        assertThat(a.prop, `is`("a"))
        assertThat(b.prop, `is`("b"))
    }
}
