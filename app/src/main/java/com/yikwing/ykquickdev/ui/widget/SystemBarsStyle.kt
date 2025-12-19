package com.yikwing.ykquickdev.ui.widget

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun SystemBarsStyle(darkIcons: Boolean) {
    val view = LocalView.current

    if (view.isInEditMode) return

    val currentWindow =
        remember(view) {
            view.context.findActivity()?.window
        } ?: return

    DisposableEffect(darkIcons) {
        val controller = WindowCompat.getInsetsController(currentWindow, view)

        // 设置图标
        controller.isAppearanceLightStatusBars = darkIcons
        controller.isAppearanceLightNavigationBars = darkIcons

        onDispose { }
    }
}

private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
