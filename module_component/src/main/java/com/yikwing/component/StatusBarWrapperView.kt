package com.yikwing.component

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.yikwing.extension.common.dp

/**
 * 状态栏高度包装器（顶部）
 * 使用 WindowInsets 获取系统状态栏高度
 * 支持自定义额外顶部间距
 *
 * 使用场景：
 * - 需要在顶部内容下方添加状态栏高度的 padding
 * - 实现沉浸式状态栏效果
 * - 支持横竖屏自动适配
 *
 * XML 使用示例：
 * ```xml
 * <com.yikwing.component.StatusBarWrapperView
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     app:extraTopPadding="10dp">
 *
 *     <!-- 你的内容视图 -->
 *     <TextView
 *         android:layout_width="match_parent"
 *         android:layout_height="wrap_content"
 *         android:text="顶部内容" />
 *
 * </com.yikwing.component.StatusBarWrapperView>
 * ```
 *
 * 代码使用示例：
 * ```kotlin
 * val wrapperView = StatusBarWrapperView(context)
 * wrapperView.extraTopPadding = 10.dp
 * ```
 *
 * 注意事项：
 * - 需要在 Activity 中启用边到边布局：
 *   WindowCompat.setDecorFitsSystemWindows(window, false)
 * - 该组件会自动处理顶部 padding，无需手动计算
 * - 支持动态修改额外间距
 *
 * @see BottomNavBarWrapperView 底部导航栏包装器
 */
class StatusBarWrapperView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : WindowInsetsWrapperView(context, attrs, defStyleAttr) {
        var extraTopPadding: Int
            get() = extraPadding
            set(value) {
                extraPadding = value
                requestInsetsUpdate()
            }

        init {
            context.obtainStyledAttributes(attrs, R.styleable.StatusBarWrapperView).apply {
                extraPadding =
                    getDimensionPixelSize(
                        R.styleable.StatusBarWrapperView_extraTopPadding,
                        10.dp,
                    )
                recycle()
            }
        }

        override fun applyInsetsPadding(
            view: View,
            insets: WindowInsetsCompat,
        ) {
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.updatePadding(top = statusBarHeight + extraPadding)
        }
    }