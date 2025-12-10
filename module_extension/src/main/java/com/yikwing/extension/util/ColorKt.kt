package com.yikwing.extension.util

import android.util.SparseIntArray
import androidx.core.graphics.toColorInt
import com.yikwing.extension.plus.getOrPut

/**
 * Alpha 值缓存，避免重复计算
 * Key: 百分比透明度 (0-100)
 * Value: 对应的 0-255 透明度值
 */
private val alphaValueCache = SparseIntArray()

/**
 * 为颜色字符串添加透明度
 *
 * 将百分比透明度 (0-100) 转换为 Android 颜色值的 Alpha 通道 (0-255)
 *
 * @param alpha 透明度百分比，范围 0-100
 *   - 0: 完全透明
 *   - 100: 完全不透明
 *   - 超出范围会自动限制到 [0, 100]
 *
 * @return Android 颜色值 (ARGB 格式)
 *
 * @throws IllegalArgumentException 如果颜色字符串格式不正确
 *
 * 使用示例:
 * ```kotlin
 * "#FF0000".alphaColor(50)  // 返回 50% 透明的红色
 * "FF0000".alphaColor(80)   // 返回 80% 透明的红色
 * "#00FF00".alphaColor(0)   // 返回完全透明的绿色
 * "#0000FF".alphaColor(100) // 返回完全不透明的蓝色
 * ```
 */
fun String.alphaColor(alpha: Int): Int {
    // 将透明度限制在 0-100 范围内
    val clampedAlpha = alpha.coerceIn(0, 100)

    // 从缓存获取或计算 Alpha 值 (0-255)
    // 公式: (百分比 * 255 + 50) / 100
    // +50 是为了四舍五入，例如: (50 * 255 + 50) / 100 = 128
    val alphaValue =
        alphaValueCache.getOrPut(clampedAlpha) {
            (clampedAlpha * 255 + 50) / 100
        }

    // 将 Alpha 值转换为两位十六进制字符串 (例如: 128 -> "80")
    val hexAlpha = alphaValue.toString(16).padStart(2, '0')

    // 构建带透明度的颜色字符串 (格式: #AARRGGBB)
    // 使用 removePrefix 简化逻辑，统一处理带 # 和不带 # 的情况
    val colorStringWithAlpha = "#$hexAlpha${this.removePrefix("#")}"

    // 转换为 Android 颜色值
    return colorStringWithAlpha.toColorInt()
}
