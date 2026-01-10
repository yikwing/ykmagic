package com.yikwing.extension.view

import android.graphics.Outline
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewOutlineProvider

/** 有底色圆角 */
fun View.backGroundRadiusColor(
    color: Int,
    radius: Number,
) {
    background =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius.toFloat()
        }
}

/** 单独设置圆角 */
fun View.backGroundRadiusColor(
    color: Int,
    topLeftRadius: Number,
    topRightRadius: Number,
    bottomRightRadius: Number,
    bottomLeftRadius: Number,
) {
    background =
        GradientDrawable().apply {
            setColor(color)
            cornerRadii =
                floatArrayOf(
                    topLeftRadius.toFloat(),
                    topLeftRadius.toFloat(),
                    topRightRadius.toFloat(),
                    topRightRadius.toFloat(),
                    bottomRightRadius.toFloat(),
                    bottomRightRadius.toFloat(),
                    bottomLeftRadius.toFloat(),
                    bottomLeftRadius.toFloat(),
                )
        }
}

/** 描边圆角 */
fun View.backGroundStroke(
    strokeWidth: Int,
    strokeColor: Int,
    radius: Number,
) {
    background =
        GradientDrawable().apply {
            setStroke(strokeWidth, strokeColor)
            cornerRadius = radius.toFloat()
        }
}

/** 裁剪圆角 */
fun View.clipRoundCorners(radius: Number) {
    outlineProvider =
        object : ViewOutlineProvider() {
            override fun getOutline(
                view: View,
                outline: Outline,
            ) {
                outline.setRoundRect(0, 0, view.width, view.height, radius.toFloat())
            }
        }
    clipToOutline = true
}
