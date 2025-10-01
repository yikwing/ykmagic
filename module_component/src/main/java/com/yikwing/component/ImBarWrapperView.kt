package com.yikwing.component

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.yikwing.extension.common.dp

/**
 * 沉浸式状态栏高度包装器
 * 使用 WindowInsets 获取系统状态栏高度
 * 支持自定义额外顶部间距
 */
class ImBarWrapperView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : FrameLayout(context, attrs, defStyleAttr) {
        private val extraTopPadding = 10.dp

        init {
            // 监听WindowInsets获取状态栏高度
            setupWindowInsets()
        }

        private fun setupWindowInsets() {
            ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
                val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
                // 只更新顶部padding，保留原始的left/right/bottom
                view.updatePadding(
                    top = statusBarHeight + extraTopPadding,
                )
                insets
            }

            // 手动请求一次WindowInsets，确保立即生效
            ViewCompat.requestApplyInsets(this)
        }
    }
