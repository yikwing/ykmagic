package com.yikwing.component

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

/**
 * WindowInsets 包装器基类
 * 提供统一的 WindowInsets 处理逻辑
 * 子类只需实现具体的 insets 提取和 padding 应用逻辑
 *
 * @see StatusBarWrapperView 状态栏包装器
 * @see BottomNavBarWrapperView 底部导航栏包装器
 */
abstract class WindowInsetsWrapperView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : FrameLayout(context, attrs, defStyleAttr) {
        protected var extraPadding: Int = 0

        init {
            setupWindowInsets()
        }

        private fun setupWindowInsets() {
            ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
                applyInsetsPadding(view, insets)
                insets
            }
            ViewCompat.requestApplyInsets(this)
        }

        /**
         * 应用 insets padding
         * 子类实现具体的 padding 应用逻辑
         */
        protected abstract fun applyInsetsPadding(
            view: android.view.View,
            insets: WindowInsetsCompat,
        )

        /**
         * 请求重新应用 WindowInsets
         */
        protected fun requestInsetsUpdate() {
            ViewCompat.requestApplyInsets(this)
        }
    }