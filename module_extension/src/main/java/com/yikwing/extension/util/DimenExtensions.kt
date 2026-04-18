package com.yikwing.extension.util

import android.content.Context
import android.view.View
import androidx.annotation.DimenRes
import androidx.fragment.app.Fragment

/**
 * dimen 资源读取扩展。
 *
 * 返回值单位均为 **px**，系统已按当前 sw 桶与设备密度换算。
 *
 * 三者区别：
 * - [dimen]       → Float，用于需要浮点精度的场景（如 Paint.strokeWidth）
 * - [dimenSize]   → Int，四舍五入，至少 1px，用于 width/height
 * - [dimenOffset] → Int，截断取整，可为 0，用于 margin/padding
 */

// ========== Context ==========

fun Context.dimen(
    @DimenRes id: Int,
): Float = resources.getDimension(id)

fun Context.dimenSize(
    @DimenRes id: Int,
): Int = resources.getDimensionPixelSize(id)

fun Context.dimenOffset(
    @DimenRes id: Int,
): Int = resources.getDimensionPixelOffset(id)

// ========== View ==========

fun View.dimen(
    @DimenRes id: Int,
): Float = resources.getDimension(id)

fun View.dimenSize(
    @DimenRes id: Int,
): Int = resources.getDimensionPixelSize(id)

fun View.dimenOffset(
    @DimenRes id: Int,
): Int = resources.getDimensionPixelOffset(id)

// ========== Fragment ==========

fun Fragment.dimen(
    @DimenRes id: Int,
): Float = resources.getDimension(id)

fun Fragment.dimenSize(
    @DimenRes id: Int,
): Int = resources.getDimensionPixelSize(id)

fun Fragment.dimenOffset(
    @DimenRes id: Int,
): Int = resources.getDimensionPixelOffset(id)
