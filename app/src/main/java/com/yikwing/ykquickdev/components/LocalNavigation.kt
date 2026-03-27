package com.yikwing.ykquickdev.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

/**
 * 提供全局导航栈访问
 */
val LocalNavigator =
    compositionLocalOf<NavBackStack<NavKey>> {
        error("No NavBackStack provided")
    }
