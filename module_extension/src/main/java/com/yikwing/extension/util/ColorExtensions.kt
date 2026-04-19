package com.yikwing.extension.util

import androidx.core.graphics.toColorInt

/**
 * 为颜色字符串添加透明度
 *
 * 将百分比透明度 (0-100) 转换为 Android 颜色值的 Alpha 通道 (0-255)。
 * 通过位运算直接合成 ARGB，避免字符串拼接再解析的开销。
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
    // 百分比映射到 0-255；+50 做四舍五入，例如 (50 * 255 + 50) / 100 = 128
    val alphaValue = (alpha.coerceIn(0, 100) * 255 + 50) / 100
    val rgb = this.toColorInt() and 0x00FFFFFF
    return (alphaValue shl 24) or rgb
}
