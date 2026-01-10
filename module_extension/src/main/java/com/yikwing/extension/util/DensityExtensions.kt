package com.yikwing.extension.util

import android.content.res.Resources
import android.util.DisplayMetrics
import kotlin.math.roundToInt

private val systemResources: Resources = Resources.getSystem()
private val systemDisplayMetrics: DisplayMetrics get() = systemResources.displayMetrics

/** dp 转 px: `16f.dp` → 32f (xhdpi) */
val Float.dp: Float get() = this * systemDisplayMetrics.density

/** dp 转 px: `16.dp` → 32 (xhdpi) */
val Int.dp: Int get() = (this * systemDisplayMetrics.density).roundToInt()

/** px 转 dp: `32.px2dp` → 16 (xhdpi) */
val Number.px2dp: Int get() = (this.toFloat() / systemDisplayMetrics.density).roundToInt()
