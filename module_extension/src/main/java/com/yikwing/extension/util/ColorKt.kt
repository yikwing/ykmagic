package com.yikwing.extension.util

import android.util.SparseIntArray // Import this class in Android projects
import androidx.core.graphics.toColorInt
import com.yikwing.extension.plus.getOrPut

private val alphaValueCache = SparseIntArray()

fun String.alphaColor(alpha: Int): Int {
    val clampedAlpha = alpha.coerceIn(0, 100)

    val alphaValue =
        alphaValueCache.getOrPut(clampedAlpha) {
            (clampedAlpha * 255 + 50) / 100
        }

    val hexAlpha = alphaValue.toString(16).padStart(2, '0')

    val colorStringWithAlpha =
        if (this.startsWith("#")) {
            "#$hexAlpha${this.substring(1)}"
        } else {
            "#$hexAlpha$this"
        }

    return colorStringWithAlpha.toColorInt()
}
