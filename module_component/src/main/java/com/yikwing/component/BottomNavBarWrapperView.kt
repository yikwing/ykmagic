package com.yikwing.component

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

/**
 * 底部导航栏高度包装器
 * 使用 WindowInsets 获取系统底部导航栏高度
 * 支持自定义额外底部间距
 *
 * 使用场景：
 * - 需要在底部内容上方添加导航栏高度的 padding
 * - 适配手势导航和三键导航
 * - 支持横竖屏自动适配
 *
 * XML 使用示例：
 * ```xml
 * <com.yikwing.component.BottomNavBarWrapperView
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     app:extraBottomPadding="10dp">
 *
 *     <!-- 你的内容视图 -->
 *     <TextView
 *         android:layout_width="match_parent"
 *         android:layout_height="wrap_content"
 *         android:text="底部内容" />
 *
 * </com.yikwing.component.BottomNavBarWrapperView>
 * ```
 *
 * 代码使用示例：
 * ```kotlin
 * val wrapperView = BottomNavBarWrapperView(context)
 * wrapperView.extraBottomPadding = 10.dp
 * ```
 *
 * 注意事项：
 * - 需要在 Activity 中启用边到边布局：
 *   WindowCompat.setDecorFitsSystemWindows(window, false)
 * - 该组件会自动处理底部 padding，无需手动计算
 * - 支持动态修改额外间距
 *
 * @see StatusBarWrapperView 状态栏包装器
 */
class BottomNavBarWrapperView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : WindowInsetsWrapperView(context, attrs, defStyleAttr) {
        var extraBottomPadding: Int
            get() = extraPadding
            set(value) {
                extraPadding = value
                requestInsetsUpdate()
            }

        init {
            context.obtainStyledAttributes(attrs, R.styleable.BottomNavBarWrapperView).apply {
                extraPadding =
                    getDimensionPixelSize(
                        R.styleable.BottomNavBarWrapperView_extraBottomPadding,
                        0,
                    )
                recycle()
            }
        }

        override fun applyInsetsPadding(
            view: View,
            insets: WindowInsetsCompat,
        ) {
            val navBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            view.updatePadding(bottom = navBarHeight + extraPadding)
        }
    }