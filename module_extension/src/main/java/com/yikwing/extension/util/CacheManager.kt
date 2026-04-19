package com.yikwing.extension.util

import android.os.SystemClock
import java.util.LinkedHashMap

/**
 * 线程安全的内存缓存管理器（LRU + TTL）。
 *
 * ## 特性
 * - **任意类型存储**：值以 `Any` 保存；调用侧需保证同一个 `key` 对应的类型一致。
 * - **LRU 淘汰**：基于 `LinkedHashMap(accessOrder = true)`，达到上限后优先淘汰"最久未访问"的条目。
 * - **TTL 过期**：使用单调时钟 `SystemClock.elapsedRealtime()` 计算过期时间，避免系统时间回拨导致的异常。
 * - **线程安全**：所有读写都通过同一把锁保护。
 *
 * ## TTL 语义
 * - `ttlMillis == 0` 表示永不过期。
 * - 过期条目会在访问时被惰性清理；同时也会做低频全量清理以避免长期滞留。
 *
 * ## LRU 刷新行为
 * - `get()` / `getOrPut()` 命中会刷新访问顺序（影响 LRU）。
 * - `contains()` 命中也会刷新访问顺序（视为一次合理访问）。
 *
 * ## 使用示例
 * ```kotlin
 * // 写入：缓存 10 秒
 * CacheManager.put("user_profile", profile, ttlMillis = 10_000)
 *
 * // 读取：命中则返回，否则为 null
 * val cached: UserProfile? = CacheManager.get("user_profile")
 *
 * // 获取或计算：并发下 defaultValue 可能被计算多次，但最终只保留一次写入
 * val token = CacheManager.getOrPut("token", ttlMillis = 60_000) { fetchToken() }
 *
 * // 调整容量
 * CacheManager.setMaxSize(512)
 * ```
 *
 * @see put
 * @see get
 * @see getOrPut
 */
object CacheManager {
    private const val DEFAULT_MAX_SIZE = 256
    private const val NO_EXPIRY_ELAPSED = 0L

    /**
     * 为避免每次 get 都全量扫描过期项，做一个低频清理（仍会在访问单 key 时即时删除过期项）。
     */
    private const val CLEANUP_INTERVAL_MILLIS = 60_000L

    private data class CacheEntry(
        val value: Any,
        val expiresAtElapsed: Long, // 0 表示永不过期
    )

    private val lock = Any()

    @Volatile
    private var maxSize: Int = DEFAULT_MAX_SIZE

    private var lastCleanupElapsed: Long = 0L

    private val cache =
        LinkedHashMap<String, CacheEntry>(
            // initialCapacity =
            DEFAULT_MAX_SIZE,
            // loadFactor =
            0.75f,
            // accessOrder =
            true,
        )

    private fun nowElapsed(): Long = SystemClock.elapsedRealtime()

    private fun computeExpiresAtElapsed(
        nowElapsed: Long,
        ttlMillis: Long,
    ): Long {
        if (ttlMillis <= 0L) return NO_EXPIRY_ELAPSED
        val remaining = Long.MAX_VALUE - nowElapsed
        return if (ttlMillis >= remaining) Long.MAX_VALUE else nowElapsed + ttlMillis
    }

    private fun isExpired(
        entry: CacheEntry,
        nowElapsed: Long,
    ): Boolean {
        val expiresAt = entry.expiresAtElapsed
        return expiresAt != NO_EXPIRY_ELAPSED && nowElapsed >= expiresAt
    }

    private fun pruneExpiredLocked(nowElapsed: Long) {
        val iterator = cache.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next().value
            if (isExpired(entry, nowElapsed)) iterator.remove()
        }
        lastCleanupElapsed = nowElapsed
    }

    private fun maybePruneExpiredLocked(nowElapsed: Long) {
        if (cache.isEmpty()) return
        if (lastCleanupElapsed != 0L && nowElapsed - lastCleanupElapsed < CLEANUP_INTERVAL_MILLIS) return
        pruneExpiredLocked(nowElapsed)
    }

    private fun trimToMaxSizeLocked(nowElapsed: Long) {
        if (maxSize <= 0) {
            cache.clear()
            return
        }

        if (cache.size > maxSize) {
            // 优先清理过期项，减少误淘汰"未过期但较旧"的项
            pruneExpiredLocked(nowElapsed)
        }

        while (cache.size > maxSize) {
            val iterator = cache.entries.iterator()
            if (!iterator.hasNext()) break
            iterator.next()
            iterator.remove() // LRU（最旧、最少访问）项
        }
    }

    /**
     * 写入缓存。
     *
     * @param key 缓存键（建议全局唯一且稳定）。
     * @param value 要缓存的值。
     * @param ttlMillis 过期时间（毫秒）；`0` 表示永不过期。
     * @throws IllegalArgumentException 当 [ttlMillis] 为负数时抛出。
     */
    fun <T : Any> put(
        key: String,
        value: T,
        ttlMillis: Long = 0,
    ) {
        require(ttlMillis >= 0L) { "ttlMillis must be >= 0" }
        val now = nowElapsed()
        val expiresAt = computeExpiresAtElapsed(now, ttlMillis)
        synchronized(lock) {
            maybePruneExpiredLocked(now)
            cache[key] = CacheEntry(value = value, expiresAtElapsed = expiresAt)
            trimToMaxSizeLocked(now)
        }
    }

    /**
     * 读取缓存。
     *
     * 命中后会刷新 LRU 访问顺序；若条目已过期则会被移除并返回 `null`。
     *
     * @param key 缓存键。
     * @return 命中且未过期时返回值，否则返回 `null`。
     * @throws ClassCastException 若该 [key] 实际存储的类型与调用处声明的 `T` 不一致。
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(key: String): T? {
        val now = nowElapsed()
        synchronized(lock) {
            maybePruneExpiredLocked(now)

            // 注意：accessOrder=true 时，这里会刷新 LRU 访问顺序
            val entry = cache[key] ?: return null

            if (isExpired(entry, now)) {
                cache.remove(key)
                return null
            }

            return entry.value as T
        }
    }

    /**
     * 获取缓存值；若不存在或已过期则计算并写入。
     *
     * 命中后会刷新 LRU 访问顺序；若条目已过期则会被移除并重新计算。
     *
     * **并发语义**：为了减少锁持有时间，[defaultValue] 会在锁外执行，因此在高并发下可能被计算多次；
     * 但写入时会再次检查并仅保留一个最终值。
     *
     * @param key 缓存键。
     * @param ttlMillis 新写入值的过期时间（毫秒）；`0` 表示永不过期。
     * @param defaultValue 当未命中时用于生成默认值的函数。
     * @return 命中时返回缓存值；未命中时返回 [defaultValue] 的计算结果。
     * @throws IllegalArgumentException 当 [ttlMillis] 为负数时抛出。
     * @throws ClassCastException 若该 [key] 实际存储的类型与调用处声明的 `T` 不一致。
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getOrPut(
        key: String,
        ttlMillis: Long = 0,
        defaultValue: () -> T,
    ): T {
        require(ttlMillis >= 0L) { "ttlMillis must be >= 0" }

        val now = nowElapsed()
        synchronized(lock) {
            maybePruneExpiredLocked(now)

            val entry = cache[key]
            if (entry != null) {
                if (!isExpired(entry, now)) return entry.value as T
                cache.remove(key)
            }
        }

        // 默认值计算放到锁外，避免阻塞其它读写；并发下可能计算多次（但只会保留一次写入）。
        val computed = defaultValue()

        val now2 = nowElapsed()
        val expiresAt = computeExpiresAtElapsed(now2, ttlMillis)
        synchronized(lock) {
            val existing = cache[key]
            if (existing != null && !isExpired(existing, now2)) return existing.value as T

            cache[key] = CacheEntry(value = computed, expiresAtElapsed = expiresAt)
            trimToMaxSizeLocked(now2)
            return computed
        }
    }

    /**
     * 移除指定键的缓存条目（若不存在则无操作）。
     *
     * @param key 缓存键。
     */
    fun remove(key: String) {
        synchronized(lock) {
            cache.remove(key)
        }
    }

    /**
     * 判断是否存在未过期的缓存条目。
     *
     * **注意**：此方法通过 `cache[key]` 查询，会刷新 LRU 访问顺序。语义上 `contains` 意图
     * 是"我关心这个 key 是否还有效"，视为一次合理访问。如需严格不影响 LRU，请改用
     * [size] 观测整体状态后再决策。若发现条目已过期，会顺便移除并返回 `false`。
     *
     * @param key 缓存键。
     * @return 存在且未过期返回 `true`，否则返回 `false`。
     */
    fun contains(key: String): Boolean {
        val now = nowElapsed()
        synchronized(lock) {
            maybePruneExpiredLocked(now)
            val entry = cache[key] ?: return false
            if (isExpired(entry, now)) {
                cache.remove(key)
                return false
            }
            return true
        }
    }

    /**
     * 清空所有缓存条目。
     */
    fun clear() {
        synchronized(lock) {
            cache.clear()
            lastCleanupElapsed = 0L
        }
    }

    /**
     * 返回当前缓存条目数（不包含已过期条目）。
     *
     * 注意：该方法会执行一次过期清理以尽量保证结果准确。
     *
     * @return 当前未过期条目数。
     */
    fun size(): Int {
        val now = nowElapsed()
        synchronized(lock) {
            // size() 通常用于观测，返回值应尽量准确：这里直接清理过期项
            pruneExpiredLocked(now)
            return cache.size
        }
    }

    /**
     * 设置最大缓存条目数（LRU 上限）。
     *
     * 设置后会立即清理过期条目并按 LRU 规则裁剪到新上限。
     *
     * @param maxSize 最大条目数；`0` 表示禁用缓存（将清空并保持为空）。
     * @throws IllegalArgumentException 当 [maxSize] 为负数时抛出。
     */
    fun setMaxSize(maxSize: Int) {
        require(maxSize >= 0) { "maxSize must be >= 0" }
        val now = nowElapsed()
        synchronized(lock) {
            this.maxSize = maxSize
            pruneExpiredLocked(now)
            trimToMaxSizeLocked(now)
        }
    }
}
