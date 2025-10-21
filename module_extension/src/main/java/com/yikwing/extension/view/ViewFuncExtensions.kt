package com.yikwing.extension.view

import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * @Author yikwing
 * @Date 13/4/2022-13:33
 * @Description: View 功能扩展
 */

// ========== 点击事件相关 ==========

/**
 * 将点击事件转换为 Flow
 * @return 点击事件的 Flow，每次点击时发送当前 View
 */
fun View.clickFlow(): Flow<View> =
    callbackFlow {
        setOnClickListener { view ->
            trySend(view)
        }
        awaitClose { setOnClickListener(null) }
    }

// ========== 可见性相关 ==========

/**
 * 设置 View 可见
 */
fun View.visible() {
    visibility = View.VISIBLE
}

/**
 * 设置 View 不可见（占位）
 */
fun View.invisible() {
    visibility = View.INVISIBLE
}

/**
 * 设置 View 隐藏（不占位）
 */
fun View.gone() {
    visibility = View.GONE
}

/**
 * 根据条件设置 View 可见性
 * @param visible 是否可见
 */
fun View.visibleOrGone(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

/**
 * 根据条件设置 View 可见性（VISIBLE 或 INVISIBLE）
 * @param visible 是否可见
 */
fun View.visibleOrInvisible(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.INVISIBLE
}

// ========== 外边距相关 ==========

/**
 * 设置 View 的 margin
 * @param left 左边距，默认保持原值
 * @param top 上边距，默认保持原值
 * @param right 右边距，默认保持原值
 * @param bottom 下边距，默认保持原值
 */
fun View.setMargin(
    left: Int? = null,
    top: Int? = null,
    right: Int? = null,
    bottom: Int? = null,
) {
    updateLayoutParams<ViewGroup.MarginLayoutParams> {
        setMargins(
            left ?: leftMargin,
            top ?: topMargin,
            right ?: rightMargin,
            bottom ?: bottomMargin,
        )
    }
}

/**
 * 设置 View 的所有方向 margin 为相同值
 * @param margin 边距值
 */
fun View.setMargin(margin: Int) {
    setMargin(margin, margin, margin, margin)
}

/**
 * 设置 View 的水平和垂直 margin
 * @param horizontal 水平边距（左右）
 * @param vertical 垂直边距（上下）
 */
fun View.setMargin(
    horizontal: Int,
    vertical: Int,
) {
    setMargin(horizontal, vertical, horizontal, vertical)
}

// ========== 长宽相关 ==========

/**
 * 设置 View 的宽高
 * @param width 宽度，null 表示保持原值，支持 MATCH_PARENT、WRAP_CONTENT 或具体数值
 * @param height 高度，null 表示保持原值，支持 MATCH_PARENT、WRAP_CONTENT 或具体数值
 */
fun View.setSize(
    width: Int? = null,
    height: Int? = null,
) {
    updateLayoutParams {
        width?.let { this.width = it }
        height?.let { this.height = it }
    }
}

/**
 * 设置 View 的宽度和高度为相同值
 * @param size 宽高值
 */
fun View.setSize(size: Int) {
    setSize(size, size)
}

/**
 * 设置 View 的宽度
 * @param width 宽度，支持 MATCH_PARENT、WRAP_CONTENT 或具体数值
 */
fun View.setWidth(width: Int) {
    setSize(width = width)
}

/**
 * 设置 View 的高度
 * @param height 高度，支持 MATCH_PARENT、WRAP_CONTENT 或具体数值
 */
fun View.setHeight(height: Int) {
    setSize(height = height)
}
