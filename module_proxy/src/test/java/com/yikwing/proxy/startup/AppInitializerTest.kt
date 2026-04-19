package com.yikwing.proxy.startup

import android.app.Application
import android.content.Context
import android.util.Log
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * AppInitializer 单元测试
 *
 * 重点验证 Kahn 拓扑排序的正确性：
 * - 依赖关系由 task 的具体 Class 标识，所以每种角色用独立命名的 class。
 * - 通过 fake Initializer 在 `create` 里记录调用顺序，间接观察 private `discover` 的输出。
 * - 走构造器而非 `getInstance`，避免进程级单例污染测试之间的状态。
 */
class AppInitializerTest {
    private lateinit var application: Application
    private val runOrder = mutableListOf<String>()

    @Before
    fun setup() {
        // debug=true 路径走 Log.d；提前 stub 避免 "Method d in android.util.Log not mocked"
        mockkStatic(Log::class)
        every { Log.d(any(), any<String>()) } returns 0
        application = mockk<Application>(relaxed = true)
        runOrder.clear()
    }

    @After
    fun teardown() {
        unmockkStatic(Log::class)
    }

    // ==================== 基础 ====================

    @Test
    fun `build with no task should not crash`() {
        AppInitializer(application).build()
        assertThat(runOrder, `is`(emptyList()))
    }

    @Test
    fun `addTask should return self for chaining`() {
        val initializer = AppInitializer(application)
        val returned = initializer.addTask(TaskA(runOrder))
        assertThat(returned, sameInstance(initializer))
    }

    @Test
    fun `single task without dependency should run once`() {
        AppInitializer(application)
            .addTask(TaskA(runOrder))
            .build()
        assertThat(runOrder, `is`(listOf("A")))
    }

    // ==================== 顺序 ====================

    @Test
    fun `independent tasks should run in declared order`() {
        // Kahn 对零入度任务按 taskList 顺序入队，FIFO 弹出 ⇒ 稳定顺序
        AppInitializer(application)
            .addTask(TaskA(runOrder))
            .addTask(TaskIndep(runOrder))
            .build()
        assertThat(runOrder, `is`(listOf("A", "Indep")))
    }

    @Test
    fun `dependent task should run after its dependency`() {
        AppInitializer(application)
            .addTask(TaskB(runOrder)) // B 依赖 A
            .addTask(TaskA(runOrder))
            .build()
        assertThat(runOrder, `is`(listOf("A", "B")))
    }

    @Test
    fun `diamond dependencies should respect partial order`() {
        // 依赖图：A ← B, A ← C, B ← D, C ← D
        // 合法序列：A 最先，D 最后，B/C 居中
        AppInitializer(application)
            .addTask(TaskD(runOrder))
            .addTask(TaskB(runOrder))
            .addTask(TaskC(runOrder))
            .addTask(TaskA(runOrder))
            .build()

        assertThat(runOrder.first(), `is`("A"))
        assertThat(runOrder.last(), `is`("D"))
        assertThat(runOrder.toSet(), `is`(setOf("A", "B", "C", "D")))
        assertThat(runOrder.indexOf("B") > runOrder.indexOf("A"), `is`(true))
        assertThat(runOrder.indexOf("C") > runOrder.indexOf("A"), `is`(true))
        assertThat(runOrder.indexOf("D") > runOrder.indexOf("B"), `is`(true))
        assertThat(runOrder.indexOf("D") > runOrder.indexOf("C"), `is`(true))
    }

    // ==================== 异常 ====================

    @Test(expected = Error::class)
    fun `cycle should throw`() {
        // Cycle1 依赖 Cycle2，Cycle2 依赖 Cycle1
        AppInitializer(application)
            .addTask(Cycle1(runOrder))
            .addTask(Cycle2(runOrder))
            .build()
    }

    @Test(expected = Error::class)
    fun `task depending on unregistered class should throw cycle error`() {
        // B 依赖 A，但 A 未被 addTask —— 入度永远无法减到 0，走循环分支报错
        AppInitializer(application)
            .addTask(TaskB(runOrder))
            .build()
    }

    // ==================== debug 日志分支 ====================

    @Test
    fun `build with debug true should still produce same order`() {
        AppInitializer(application)
            .addTask(TaskB(runOrder))
            .addTask(TaskA(runOrder))
            .build(debug = true)
        assertThat(runOrder, `is`(listOf("A", "B")))
    }

    // ==================== fake 任务 ====================
    // 每种角色一个独立 class —— discover 用 `task.javaClass` 作键

    private class TaskA(
        private val order: MutableList<String>,
    ) : Initializer<Unit> {
        override fun create(context: Context) {
            order.add("A")
        }

        override fun dependencies(): Set<Class<out Initializer<*>>> = emptySet()
    }

    private class TaskB(
        private val order: MutableList<String>,
    ) : Initializer<Unit> {
        override fun create(context: Context) {
            order.add("B")
        }

        override fun dependencies(): Set<Class<out Initializer<*>>> = setOf(TaskA::class.java)
    }

    private class TaskC(
        private val order: MutableList<String>,
    ) : Initializer<Unit> {
        override fun create(context: Context) {
            order.add("C")
        }

        override fun dependencies(): Set<Class<out Initializer<*>>> = setOf(TaskA::class.java)
    }

    private class TaskIndep(
        private val order: MutableList<String>,
    ) : Initializer<Unit> {
        override fun create(context: Context) {
            order.add("Indep")
        }

        override fun dependencies(): Set<Class<out Initializer<*>>> = emptySet()
    }

    private class TaskD(
        private val order: MutableList<String>,
    ) : Initializer<Unit> {
        override fun create(context: Context) {
            order.add("D")
        }

        override fun dependencies(): Set<Class<out Initializer<*>>> =
            setOf(TaskB::class.java, TaskC::class.java)
    }

    private class Cycle1(
        private val order: MutableList<String>,
    ) : Initializer<Unit> {
        override fun create(context: Context) {
            order.add("C1")
        }

        override fun dependencies(): Set<Class<out Initializer<*>>> = setOf(Cycle2::class.java)
    }

    private class Cycle2(
        private val order: MutableList<String>,
    ) : Initializer<Unit> {
        override fun create(context: Context) {
            order.add("C2")
        }

        override fun dependencies(): Set<Class<out Initializer<*>>> = setOf(Cycle1::class.java)
    }
}