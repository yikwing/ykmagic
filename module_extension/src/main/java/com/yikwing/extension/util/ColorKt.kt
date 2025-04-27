package com.yikwing.extension.util

import androidx.core.graphics.toColorInt

fun String.alphaColor(alpha: Int): Int {
    // Ensure alpha is within the valid range [0, 100]
    val clampedAlpha = alpha.coerceIn(0, 100)

    // Calculate the hex alpha value (0-255) using integer arithmetic rounding
    // (x * numerator + denominator / 2) / denominator is a common integer rounding trick
    val alphaValue = (clampedAlpha * 255 + 50) / 100

    // Convert to hexadecimal string and pad with leading zero if needed
    val hexAlpha = alphaValue.toString(16).padStart(2, '0')

    // Replace the '#' and append the hex alpha value
    // Handle cases where the original string might not start with '#' gracefully
    val colorStringWithAlpha =
        if (this.startsWith("#")) {
            "#$hexAlpha${this.substring(1)}"
        } else {
            // If no '#', just prepend the hex alpha with '#'
            "#$hexAlpha$this"
        }

    // Convert the final string to color integer
    // Assuming toColorInt() is available in your environment
    return colorStringWithAlpha.toColorInt()
}
