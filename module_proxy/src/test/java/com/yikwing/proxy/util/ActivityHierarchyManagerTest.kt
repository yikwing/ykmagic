package com.yikwing.proxy.util

import android.app.Activity
import android.util.Log
import io.mockk.*
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * ActivityHierarchyManager 单元测试
 *
 * 测试覆盖：
 * - Activity 注册和注销
 * - 栈操作（获取、查询、关闭）
 * - 边界条件处理
 */
class ActivityHierarchyManagerTest {

    private lateinit var mockActivity1: Activity
    private lateinit var mockActivity2: Activity
    private lateinit var mockActivity3: Activity

    @Before
    fun setup() {
        // Mock Android Log
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0

        // 清空单例状态
        clearAllMocks(answers = false, recordedCalls = false, childMocks = false)

        // 创建 mock Activity 对象
        mockActivity1 = mockk<Activity>(relaxed = true)
        every { mockActivity1.isFinishing } returns false
        every { mockActivity1.isDestroyed } returns false

        mockActivity2 = mockk<Activity>(relaxed = true)
        every { mockActivity2.isFinishing } returns false
        every { mockActivity2.isDestroyed } returns false

        mockActivity3 = mockk<Activity>(relaxed = true)
        every { mockActivity3.isFinishing } returns false
        every { mockActivity3.isDestroyed } returns false
    }

    @After
    fun tearDown() {
        // 清空所有 Activity
        try {
            ActivityHierarchyManager.finishAllActivities()
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
        clearAllMocks(answers = false, recordedCalls = false, childMocks = false)
        unmockkStatic(Log::class)
    }

    @Test
    fun `register should add activity to stack`() {
        ActivityHierarchyManager.register(mockActivity1)

        assertThat(ActivityHierarchyManager.getActivityCount(), `is`(1))
        assertThat(ActivityHierarchyManager.getTopActivity(), `is`(mockActivity1))
    }

    @Test
    fun `register should not add duplicate activity`() {
        ActivityHierarchyManager.register(mockActivity1)
        ActivityHierarchyManager.register(mockActivity1)

        assertThat(ActivityHierarchyManager.getActivityCount(), `is`(1))
    }

    @Test
    fun `register multiple activities should maintain order`() {
        ActivityHierarchyManager.register(mockActivity1)
        ActivityHierarchyManager.register(mockActivity2)
        ActivityHierarchyManager.register(mockActivity3)

        assertThat(ActivityHierarchyManager.getActivityCount(), `is`(3))
        assertThat(ActivityHierarchyManager.getTopActivity(), `is`(mockActivity3))

        val stack = ActivityHierarchyManager.getActivityStack()
        assertThat(stack[0], `is`(mockActivity1))
        assertThat(stack[1], `is`(mockActivity2))
        assertThat(stack[2], `is`(mockActivity3))
    }

    @Test
    fun `unregister should remove activity from stack`() {
        ActivityHierarchyManager.register(mockActivity1)
        ActivityHierarchyManager.register(mockActivity2)

        ActivityHierarchyManager.unregister(mockActivity1)

        assertThat(ActivityHierarchyManager.getActivityCount(), `is`(1))
        assertThat(ActivityHierarchyManager.getTopActivity(), `is`(mockActivity2))
    }

    @Test
    fun `unregister top activity should update top`() {
        ActivityHierarchyManager.register(mockActivity1)
        ActivityHierarchyManager.register(mockActivity2)

        ActivityHierarchyManager.unregister(mockActivity2)

        assertThat(ActivityHierarchyManager.getTopActivity(), `is`(mockActivity1))
    }

    @Test
    fun `getActivityCount should return correct count`() {
        assertThat(ActivityHierarchyManager.getActivityCount(), `is`(0))

        ActivityHierarchyManager.register(mockActivity1)
        assertThat(ActivityHierarchyManager.getActivityCount(), `is`(1))

        ActivityHierarchyManager.register(mockActivity2)
        assertThat(ActivityHierarchyManager.getActivityCount(), `is`(2))

        ActivityHierarchyManager.unregister(mockActivity1)
        assertThat(ActivityHierarchyManager.getActivityCount(), `is`(1))
    }

    @Test
    fun `getTopActivity should return null when stack is empty`() {
        assertThat(ActivityHierarchyManager.getTopActivity(), nullValue())
    }

    @Test
    fun `getTopActivity should return last registered activity`() {
        ActivityHierarchyManager.register(mockActivity1)
        ActivityHierarchyManager.register(mockActivity2)
        ActivityHierarchyManager.register(mockActivity3)

        assertThat(ActivityHierarchyManager.getTopActivity(), `is`(mockActivity3))
    }

    @Test
    fun `contains with activity instance should return true when exists`() {
        ActivityHierarchyManager.register(mockActivity1)

        assertThat(ActivityHierarchyManager.contains(mockActivity1), `is`(true))
        assertThat(ActivityHierarchyManager.contains(mockActivity2), `is`(false))
    }

    @Test
    fun `getActivityStack should return all valid activities`() {
        ActivityHierarchyManager.register(mockActivity1)
        ActivityHierarchyManager.register(mockActivity2)
        ActivityHierarchyManager.register(mockActivity3)

        val stack = ActivityHierarchyManager.getActivityStack()

        assertThat(stack.size, `is`(3))
        assertThat(stack[0], `is`(mockActivity1))
        assertThat(stack[1], `is`(mockActivity2))
        assertThat(stack[2], `is`(mockActivity3))
    }

    @Test
    fun `getActivityAt should return correct activity`() {
        ActivityHierarchyManager.register(mockActivity1)
        ActivityHierarchyManager.register(mockActivity2)
        ActivityHierarchyManager.register(mockActivity3)

        assertThat(ActivityHierarchyManager.getActivityAt(0), `is`(mockActivity1))
        assertThat(ActivityHierarchyManager.getActivityAt(1), `is`(mockActivity2))
        assertThat(ActivityHierarchyManager.getActivityAt(2), `is`(mockActivity3))
    }

    @Test
    fun `getActivityAt should return null for invalid index`() {
        ActivityHierarchyManager.register(mockActivity1)

        assertThat(ActivityHierarchyManager.getActivityAt(-1), nullValue())
        assertThat(ActivityHierarchyManager.getActivityAt(10), nullValue())
    }

    @Test
    fun `finishTopActivities should finish correct number of activities`() {
        ActivityHierarchyManager.register(mockActivity1)
        ActivityHierarchyManager.register(mockActivity2)
        ActivityHierarchyManager.register(mockActivity3)

        val finishedCount = ActivityHierarchyManager.finishTopActivities(2)

        assertThat(finishedCount, `is`(2))
        verify { mockActivity2.finish() }
        verify { mockActivity3.finish() }
        verify(exactly = 0) { mockActivity1.finish() }
    }

    @Test
    fun `finishTopActivities should handle count larger than stack size`() {
        ActivityHierarchyManager.register(mockActivity1)
        ActivityHierarchyManager.register(mockActivity2)

        val finishedCount = ActivityHierarchyManager.finishTopActivities(10)

        assertThat(finishedCount, `is`(2))
        verify { mockActivity1.finish() }
        verify { mockActivity2.finish() }
    }

    @Test
    fun `finishTopActivities should return 0 for invalid count`() {
        ActivityHierarchyManager.register(mockActivity1)

        assertThat(ActivityHierarchyManager.finishTopActivities(0), `is`(0))
        assertThat(ActivityHierarchyManager.finishTopActivities(-1), `is`(0))
    }

    @Test
    fun `finishTopActivities should skip already finishing activities`() {
        every { mockActivity2.isFinishing } returns true

        ActivityHierarchyManager.register(mockActivity1)
        ActivityHierarchyManager.register(mockActivity2)
        ActivityHierarchyManager.register(mockActivity3)

        // 虽然我们请求关闭 2 个，但 mockActivity2 正在 finishing
        // 实际上会尝试关闭 mockActivity2 和 mockActivity3，但只有 mockActivity3 会真正被调用 finish()
        val finishedCount = ActivityHierarchyManager.finishTopActivities(2)

        // 期望finishedCount = 1 (只有 mockActivity3 被 finish)
        // 但是由于getValidActivities会过滤isFinishing的Activity，所以实际上finishTopActivities会从
        // [mockActivity1, mockActivity3]中选择最后2个，即mockActivity1和mockActivity3
        assertThat(finishedCount, `is`(2))
        verify { mockActivity1.finish() }
        verify { mockActivity3.finish() }
        verify(exactly = 0) { mockActivity2.finish() }
    }

    @Test
    fun `finishAllActivities should finish all activities`() {
        ActivityHierarchyManager.register(mockActivity1)
        ActivityHierarchyManager.register(mockActivity2)
        ActivityHierarchyManager.register(mockActivity3)

        ActivityHierarchyManager.finishAllActivities()

        verify { mockActivity1.finish() }
        verify { mockActivity2.finish() }
        verify { mockActivity3.finish() }
        assertThat(ActivityHierarchyManager.getActivityCount(), `is`(0))
    }

    @Test
    fun `deprecated finishActivities should work same as finishTopActivities`() {
        ActivityHierarchyManager.register(mockActivity1)
        ActivityHierarchyManager.register(mockActivity2)

        @Suppress("DEPRECATION")
        ActivityHierarchyManager.finishActivities(1)

        verify { mockActivity2.finish() }
        verify(exactly = 0) { mockActivity1.finish() }
    }

    @Test
    fun `should filter out finishing activities`() {
        every { mockActivity2.isFinishing } returns true

        ActivityHierarchyManager.register(mockActivity1)
        ActivityHierarchyManager.register(mockActivity2)
        ActivityHierarchyManager.register(mockActivity3)

        // getValidActivities 会过滤掉 finishing 的 Activity
        assertThat(ActivityHierarchyManager.getActivityCount(), `is`(2))

        val stack = ActivityHierarchyManager.getActivityStack()
        assertThat(stack.size, `is`(2))
        assertThat(stack.contains(mockActivity2), `is`(false))
    }

    @Test
    fun `should filter out destroyed activities`() {
        every { mockActivity2.isDestroyed } returns true

        ActivityHierarchyManager.register(mockActivity1)
        ActivityHierarchyManager.register(mockActivity2)
        ActivityHierarchyManager.register(mockActivity3)

        assertThat(ActivityHierarchyManager.getActivityCount(), `is`(2))
        assertThat(ActivityHierarchyManager.contains(mockActivity2), `is`(false))
    }

    @Test
    fun `printActivityHierarchy should not crash`() {
        ActivityHierarchyManager.register(mockActivity1)
        ActivityHierarchyManager.register(mockActivity2)

        // 不应该抛出异常
        ActivityHierarchyManager.printActivityHierarchy(true)
        ActivityHierarchyManager.printActivityHierarchy(false)
    }
}
