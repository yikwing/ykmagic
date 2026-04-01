package com.yikwing.compose.window

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
        controller.isAppearanceLightStatusBars = statusBarDarkIcons
        controller.isAppearanceLightNavigationBars = navigationBarDarkIcons
        onDispose { }
    }
}

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
