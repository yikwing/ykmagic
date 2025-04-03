package com.yikwing.extension.common

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.TypedValue

// 获取系统 DisplayMetrics，避免重复创建
private val systemDisplayMetrics: DisplayMetrics
    get() = Resources.getSystem().displayMetrics

// 屏幕尺寸
val SCREEN_WIDTH: Int
    @JvmName("SCREEN_WIDTH")
    get() = systemDisplayMetrics.widthPixels

val SCREEN_HEIGHT: Int
    @JvmName("SCREEN_HEIGHT")
    get() = systemDisplayMetrics.heightPixels

// 屏幕密度
val SCREEN_DPI: Float
    @JvmName("SCREEN_DPI")
    get() = systemDisplayMetrics.density

// 单位转换 (dp, sp, px)
val Float.dp: Float
    @JvmName("dp2px")
    get() =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this,
            systemDisplayMetrics,
        )

val Int.dp: Int
    @JvmName("dp2px")
    get() =
        TypedValue
            .applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                this.toFloat(),
                systemDisplayMetrics,
            ).toInt()

val Float.sp: Float
    @JvmName("sp2px")
    get() =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            this,
            systemDisplayMetrics,
        )

val Int.sp: Int
    @JvmName("sp2px")
    get() =
        TypedValue
            .applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                this.toFloat(),
                systemDisplayMetrics,
            ).toInt()

val Number.px: Number
    get() = this

val Number.px2dp: Int
    @JvmName("px2dp")
    get() = (this.toFloat() / systemDisplayMetrics.density).toInt()

val Number.px2sp: Int
    @JvmName("px2sp")
    get() = (this.toFloat() / systemDisplayMetrics.scaledDensity).toInt()

// 系统栏高度 (状态栏, 导航栏, ActionBar)
val STATUS_BAR_HEIGHT: Int
    @JvmName("STATUS_BAR_HEIGHT")
    get() {
        val resourceId =
            Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android")
        return Resources.getSystem().getDimensionPixelSize(resourceId)
    }

val NAVIGATION_BAR_HEIGHT: Int
    @JvmName("NAVIGATION_BAR_HEIGHT")
    get() {
        val resourceId =
            Resources.getSystem().getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId != 0) {
            Resources.getSystem().getDimensionPixelOffset(resourceId)
        } else {
            0
        }
    }

val Context.ACTION_BAR_HEIGHT: Int
    @JvmName("ACTION_BAR_HEIGHT")
    get() {
        val tv = TypedValue()
        return if (theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
        } else {
            0
        }
    }
