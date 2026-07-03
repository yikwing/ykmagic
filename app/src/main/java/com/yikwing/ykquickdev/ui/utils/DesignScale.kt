package com.yikwing.ykquickdev.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 设计稿缩放工具
 * 基于 375dp 设计稿宽度进行等比缩放
 *
 * 适用场景：
 * - 需要严格按设计稿等比缩放的 UI（如首页 Banner、卡片布局）
 *
 * 不适用场景：
 * - 文字大小（应使用 sp 并遵循系统字体缩放）
 * - 最小触摸区域（应保持 48dp 不缩放）
 * - 系统组件（如 TopAppBar 高度）
 *
 * 使用示例：
 * ```
 * Box(modifier = Modifier.size(200.sdp, 100.sdp))
 * Box(modifier = Modifier.height(60.5.sdp))
 * ```
 */

val LocalDesignScale = staticCompositionLocalOf { 1f }

private const val DESIGN_WIDTH_DP = 375f

/**
 * 提供设计稿缩放上下文
 * 在根布局使用，子组件可通过 designDp() 获取缩放后的尺寸
 */
@Composable
fun ProvideDesignScale(content: @Composable () -> Unit) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val density = LocalDensity.current

    val designScale =
        remember(screenWidthDp) {
            // 这里的逻辑可以根据你的项目需求灵活调整
            if (screenWidthDp > 600) {
                // 平板模式下：限制最大缩放比例，避免 UI 元素过大
                (screenWidthDp / DESIGN_WIDTH_DP).coerceAtMost(1.4f)
            } else {
                screenWidthDp / DESIGN_WIDTH_DP
            }
        }

    // 关键点：创建一个新的 Density 实例，强制将 fontScale 设为固定值（如 1.0）
    val fixedDensity =
        Density(
            density = density.density,
            // 强制字体缩放为 1.0
            fontScale = 1f,
        )

    CompositionLocalProvider(
        LocalDesignScale provides designScale,
        LocalDensity provides fixedDensity,
    ) {
        content()
    }
}

val Int.sdp: Dp
    @Composable get() = (this * LocalDesignScale.current).dp

val Float.sdp: Dp
    @Composable get() = (this * LocalDesignScale.current).dp
