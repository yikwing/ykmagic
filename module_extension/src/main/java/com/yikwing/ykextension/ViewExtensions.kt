package com.yikwing.ykextension

import android.graphics.drawable.GradientDrawable
import android.view.View

/**
 * 有底色圆角
 * */
fun View.backGroundRadiusColor(
    color: Int,
    radius: Float
) {
    background = (
            GradientDrawable().apply {
                setColor(color)
                cornerRadius = radius
            })
}

/**
 * 单独设置圆角
 */
fun View.backGroundRadiusColor(
    color: Int,
    topLeftRadius: Float,
    topRightRadius: Float,
    bottomRightRadius: Float,
    bottomLeftRadius: Float
) {
    background = (
            GradientDrawable().apply {
                setColor(color)
                cornerRadii = floatArrayOf(
                    topLeftRadius, topLeftRadius,
                    topRightRadius, topRightRadius,
                    bottomRightRadius, bottomRightRadius,
                    bottomLeftRadius, bottomLeftRadius
                )
            })
}

/**
 * 描边圆角
 * */
fun View.backGroundStroke(
    strokeWidth: Int,
    strokeColor: Int,
    radius: Float
) {
    background = (
            GradientDrawable().apply {
                setStroke(strokeWidth, strokeColor)
                cornerRadius = radius
            })
}