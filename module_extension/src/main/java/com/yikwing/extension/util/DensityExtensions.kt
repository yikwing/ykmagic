package com.yikwing.extension.util

import android.content.res.Resources
import android.util.DisplayMetrics
import kotlin.math.roundToInt

/**
 * dp/px 转换扩展（顶层属性）
 *
 * **使用限制**：基于 [Resources.getSystem]，只反映**系统默认**的
 * [DisplayMetrics.density]，不随应用配置变化（字体缩放、分屏、多窗口、
 * 折叠屏切换等场景下可能与 `context.resources.displayMetrics` 不一致）。
 *
 * 适用：全局常量计算、独立于 Activity 的工具场景、已知配置与系统默认一致时。
 *
 * 不适用：配置敏感场景（分屏/多窗口/字体缩放）。此时请改用
 * `context.resources.displayMetrics.density` 手动计算。
 */
private val systemResources: Resources = Resources.getSystem()
private val systemDisplayMetrics: DisplayMetrics get() = systemResources.displayMetrics

/** dp 转 px: `16f.dp` → 32f (xhdpi)；见文件头的使用限制 */
val Float.dp: Float get() = this * systemDisplayMetrics.density

/** dp 转 px: `16.dp` → 32 (xhdpi)；见文件头的使用限制 */
val Int.dp: Int get() = (this * systemDisplayMetrics.density).roundToInt()

/** px 转 dp: `32.px2dp` → 16 (xhdpi)；见文件头的使用限制 */
val Number.px2dp: Int get() = (this.toFloat() / systemDisplayMetrics.density).roundToInt()
