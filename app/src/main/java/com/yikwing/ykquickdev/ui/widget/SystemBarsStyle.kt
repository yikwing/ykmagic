package com.yikwing.ykquickdev.ui.widget

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * 控制系统状态栏和导航栏图标颜色
 *
 * @param statusBarDarkIcons 状态栏是否使用深色图标（浅色背景用 true）
 * @param navigationBarDarkIcons 导航栏是否使用深色图标，默认与状态栏一致
 *
 * 注意：此函数会在 Composable 离开组合时恢复原始状态
 */
@Composable
fun SystemBarsStyle(
    statusBarDarkIcons: Boolean,
    navigationBarDarkIcons: Boolean = statusBarDarkIcons,
) {
    val view = LocalView.current

    if (view.isInEditMode) return

    val currentWindow =
        remember(view) {
            view.context.findActivity()?.window
        } ?: return

    DisposableEffect(statusBarDarkIcons, navigationBarDarkIcons) {
        val controller = WindowCompat.getInsetsController(currentWindow, view)

        // 设置新状态
        controller.isAppearanceLightStatusBars = statusBarDarkIcons
        controller.isAppearanceLightNavigationBars = navigationBarDarkIcons

        onDispose { }
    }
}

/**
 * 向上遍历 Context 链查找 Activity
 *
 * @param maxDepth 最大遍历深度，防止循环引用
 * @return 找到的 Activity，未找到返回 null
 */
private fun Context.findActivity(maxDepth: Int = 20): Activity? {
    var context = this
    var depth = 0

    while (context is ContextWrapper && depth < maxDepth) {
        if (context is Activity) return context
        context = context.baseContext
        depth++
    }

    return null
}
