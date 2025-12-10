package com.yikwing.extension.plus

import android.util.SparseIntArray

/**
 * 获取指定 key 的值，如果不存在则计算并存储默认值
 *
 * 类似于 Kotlin 标准库的 `Map.getOrPut`，但针对 SparseIntArray 优化
 *
 * @param key 要查找的键
 * @param defaultValue 当 key 不存在时，用于计算默认值的 lambda 表达式
 *   - 只有在 key 不存在时才会执行
 *   - 计算结果会自动存储到 SparseIntArray 中
 *
 * @return 如果 key 存在，返回已有的值；否则返回计算并存储的默认值
 *
 * 使用示例:
 * ```kotlin
 * val cache = SparseIntArray()
 *
 * // 第一次调用：计算并缓存
 * val value1 = cache.getOrPut(10) { 10 * 255 / 100 }  // 返回 25，并存储
 *
 * // 第二次调用：直接从缓存获取
 * val value2 = cache.getOrPut(10) { 10 * 255 / 100 }  // 返回 25，不执行 lambda
 * ```
 *
 * 性能优势:
 * - 使用 `indexOfKey` + `valueAt` 避免二次查找
 * - 相比 `get(key, defaultValue)` 支持延迟计算
 * - 避免装箱，性能优于 `HashMap<Int, Int>`
 */
inline fun SparseIntArray.getOrPut(
    key: Int,
    defaultValue: () -> Int,
): Int {
    val index = indexOfKey(key)
    return if (index >= 0) {
        valueAt(index)
    } else {
        val value = defaultValue()
        put(key, value)
        value
    }
}
