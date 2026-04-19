package com.yikwing.extension.util

import android.os.SystemClock
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * CacheManager 单元测试
 *
 * SystemClock.elapsedRealtime() 通过 mockkStatic 伪造时间轴，以便
 * 确定性地验证 TTL 过期与 LRU 淘汰。
 */
class CacheManagerTest {
    private var fakeTime = 0L

    @Before
    fun setup() {
        mockkStatic(SystemClock::class)
        every { SystemClock.elapsedRealtime() } answers { fakeTime }
        fakeTime = 1_000L
        CacheManager.clear()
        CacheManager.setMaxSize(256)
    }

    @After
    fun teardown() {
        CacheManager.clear()
        unmockkStatic(SystemClock::class)
    }

    // ==================== put & get ====================

    @Test
    fun `put and get should store and retrieve value`() {
        CacheManager.put("key", "value")
        assertThat(CacheManager.get<String>("key"), `is`("value"))
    }

    @Test
    fun `get should return null for missing key`() {
        assertThat(CacheManager.get<String>("missing"), nullValue())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `put with negative ttl should throw`() {
        CacheManager.put("key", "value", ttlMillis = -1)
    }

    // ==================== TTL ====================

    @Test
    fun `put with ttl=0 should never expire`() {
        CacheManager.put("key", "value", ttlMillis = 0)
        fakeTime += 10_000_000L
        assertThat(CacheManager.get<String>("key"), `is`("value"))
    }

    @Test
    fun `entry should expire after ttl`() {
        CacheManager.put("key", "value", ttlMillis = 100)
        fakeTime += 50
        assertThat(CacheManager.get<String>("key"), `is`("value"))
        fakeTime += 51
        assertThat(CacheManager.get<String>("key"), nullValue())
    }

    @Test
    fun `get should remove expired entry`() {
        CacheManager.put("key", "value", ttlMillis = 100)
        fakeTime += 200
        CacheManager.get<String>("key")
        assertThat(CacheManager.contains("key"), `is`(false))
    }

    // ==================== getOrPut ====================

    @Test
    fun `getOrPut should compute default when missing`() {
        val result = CacheManager.getOrPut("key") { "computed" }
        assertThat(result, `is`("computed"))
        assertThat(CacheManager.get<String>("key"), `is`("computed"))
    }

    @Test
    fun `getOrPut should return cached value when hit`() {
        CacheManager.put("key", "cached")
        var invocations = 0
        val result =
            CacheManager.getOrPut("key") {
                invocations++
                "computed"
            }
        assertThat(result, `is`("cached"))
        assertThat(invocations, `is`(0))
    }

    @Test
    fun `getOrPut should recompute when expired`() {
        CacheManager.put("key", "old", ttlMillis = 100)
        fakeTime += 200
        val result = CacheManager.getOrPut("key", ttlMillis = 100) { "new" }
        assertThat(result, `is`("new"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `getOrPut with negative ttl should throw`() {
        CacheManager.getOrPut("key", ttlMillis = -1) { "value" }
    }

    // ==================== contains ====================

    @Test
    fun `contains should return false for missing key`() {
        assertThat(CacheManager.contains("missing"), `is`(false))
    }

    @Test
    fun `contains should return true for existing key`() {
        CacheManager.put("key", "value")
        assertThat(CacheManager.contains("key"), `is`(true))
    }

    @Test
    fun `contains should return false for expired entry and remove it`() {
        CacheManager.put("key", "value", ttlMillis = 100)
        fakeTime += 200
        assertThat(CacheManager.contains("key"), `is`(false))
        assertThat(CacheManager.size(), `is`(0))
    }

    // ==================== remove / clear / size ====================

    @Test
    fun `remove should delete entry`() {
        CacheManager.put("key", "value")
        CacheManager.remove("key")
        assertThat(CacheManager.get<String>("key"), nullValue())
    }

    @Test
    fun `remove nonexistent key should be no-op`() {
        CacheManager.remove("missing")
        assertThat(CacheManager.size(), `is`(0))
    }

    @Test
    fun `clear should delete all entries`() {
        CacheManager.put("a", 1)
        CacheManager.put("b", 2)
        CacheManager.clear()
        assertThat(CacheManager.size(), `is`(0))
    }

    @Test
    fun `size should exclude expired entries`() {
        CacheManager.put("a", 1, ttlMillis = 100)
        CacheManager.put("b", 2, ttlMillis = 300)
        assertThat(CacheManager.size(), `is`(2))
        fakeTime += 200
        assertThat(CacheManager.size(), `is`(1))
    }

    // ==================== LRU ====================

    @Test
    fun `setMaxSize should trim oldest entries`() {
        CacheManager.setMaxSize(3)
        CacheManager.put("a", 1)
        CacheManager.put("b", 2)
        CacheManager.put("c", 3)
        CacheManager.put("d", 4)
        assertThat(CacheManager.contains("a"), `is`(false))
        assertThat(CacheManager.contains("d"), `is`(true))
        assertThat(CacheManager.size(), `is`(3))
    }

    @Test
    fun `setMaxSize to 0 should clear all`() {
        CacheManager.put("a", 1)
        CacheManager.put("b", 2)
        CacheManager.setMaxSize(0)
        assertThat(CacheManager.size(), `is`(0))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `setMaxSize negative should throw`() {
        CacheManager.setMaxSize(-1)
    }

    @Test
    fun `get should refresh LRU access order`() {
        CacheManager.setMaxSize(3)
        CacheManager.put("a", 1)
        CacheManager.put("b", 2)
        CacheManager.put("c", 3)
        CacheManager.get<Int>("a") // 把 a 刷到最新
        CacheManager.put("d", 4) // 超容量，淘汰最旧的 b
        assertThat(CacheManager.contains("a"), `is`(true))
        assertThat(CacheManager.contains("b"), `is`(false))
        assertThat(CacheManager.contains("c"), `is`(true))
        assertThat(CacheManager.contains("d"), `is`(true))
    }

    @Test
    fun `contains should refresh LRU access order`() {
        CacheManager.setMaxSize(3)
        CacheManager.put("a", 1)
        CacheManager.put("b", 2)
        CacheManager.put("c", 3)
        CacheManager.contains("a") // 新实现下 contains 也算一次访问
        CacheManager.put("d", 4)
        assertThat(CacheManager.contains("a"), `is`(true))
        assertThat(CacheManager.contains("b"), `is`(false))
    }
}