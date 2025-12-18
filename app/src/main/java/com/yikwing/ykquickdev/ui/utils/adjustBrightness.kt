package com.yikwing.ykquickdev.ui.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpaces

/**
 * 使用 Oklab 空间将颜色调暗
 * @param factor 亮度因子。默认 0.8f 代表暗 20%；1.2f 代表亮 20%
 */
fun Color.adjustBrightness(factor: Float = 0.8f): Color {
    // 1. 转换到 Oklab 空间
    val oklabColor = this.convert(ColorSpaces.Oklab)

    // 2. 提取分量 (v1=L, v2=a, v3=b)
    val l = oklabColor.red // 在 Oklab 空间中，red 分量映射为 L
    val a = oklabColor.green // green 分量映射为 a
    val b = oklabColor.blue // blue 分量映射为 b

    // 3. 仅调整 L (亮度) 分量，并限制在 0.0 ~ 1.0 之间
    val newL = (l * factor).coerceIn(0f, 1f)

    // 4. 构造新颜色并转回 sRGB (Compose 会自动处理显示兼容)
    return Color(
        red = newL,
        green = a,
        blue = b,
        alpha = this.alpha,
        colorSpace = ColorSpaces.Oklab,
    ).convert(ColorSpaces.Srgb)
}
