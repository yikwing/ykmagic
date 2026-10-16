package com.yikwing.compose.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
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

// Upper bound so tablets / landscape phones don't blow UI up proportionally
private const val MAX_DESIGN_SCALE = 1.4f

// Respect the system font size for accessibility, but cap it so fixed-size layouts don't break
private const val MAX_FONT_SCALE = 1.3f

/**
 * 提供设计稿缩放上下文
 * 在根布局使用，子组件可通过 sdp 获取缩放后的尺寸
 */
@Composable
fun ProvideDesignScale(content: @Composable () -> Unit) {
    // containerDpSize is the real window size; Configuration.screenWidthDp is rounded and
    // its inset handling varies with targetSdk
    val windowWidthDp = LocalWindowInfo.current.containerDpSize.width.value
    val designScale = calculateDesignScale(windowWidthDp)

    CompositionLocalProvider(
        LocalDesignScale provides designScale,
        LocalDensity provides capFontScale(LocalDensity.current),
    ) {
        content()
    }
}

val Int.sdp: Dp
    @Composable @ReadOnlyComposable get() = (this * LocalDesignScale.current).dp

val Float.sdp: Dp
    @Composable @ReadOnlyComposable get() = (this * LocalDesignScale.current).dp

internal fun calculateDesignScale(widthDp: Float): Float = (widthDp / DESIGN_WIDTH_DP).coerceAtMost(MAX_DESIGN_SCALE)

/**
 * Returns [density] untouched when its fontScale is within [MAX_FONT_SCALE], so the platform's
 * non-linear font scaling (API 34+) is preserved; otherwise a linear Density capped at the max.
 */
internal fun capFontScale(density: Density): Density =
    if (density.fontScale <= MAX_FONT_SCALE) {
        density
    } else {
        Density(density = density.density, fontScale = MAX_FONT_SCALE)
    }
