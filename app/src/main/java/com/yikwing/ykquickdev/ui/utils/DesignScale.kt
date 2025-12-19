package com.yikwing.ykquickdev.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
 * ```
 */
@Immutable
data class DesignScale(
    val scale: Float,
)

val LocalDesignScale =
    staticCompositionLocalOf<DesignScale> {
        error("DesignScale not provided. Please wrap your content with ProvideDesignScale.")
    }

private const val DESIGN_WIDTH_DP = 375f

/**
 * 提供设计稿缩放上下文
 * 在根布局使用，子组件可通过 designDp() 获取缩放后的尺寸
 */
@Composable
fun ProvideDesignScale(content: @Composable () -> Unit) {
    // 读取 LocalConfiguration 确保分屏/窗口变化时触发重组
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val density = LocalDensity.current.density
    val screenWidthPx = context.resources.displayMetrics.widthPixels
    // 预计算缩放因子：screenWidthDp / 375
    val designScale =
        remember(screenWidthPx, density, configuration.screenWidthDp) {
            val screenWidthDp = screenWidthPx / density
            DesignScale(scale = screenWidthDp / DESIGN_WIDTH_DP)
        }
    CompositionLocalProvider(LocalDesignScale provides designScale) {
        content()
    }
}

/**
 * 将设计稿尺寸转换为当前屏幕的实际 Dp
 * 计算公式：designValue * scale
 * @param value 设计稿中的 dp 值
 * @return 缩放后的 Dp
 */
@Composable
fun designDp(value: Int): Dp {
    val scale = LocalDesignScale.current.scale
    return (value * scale).dp
}

@Composable
fun designDp(value: Float): Dp {
    val scale = LocalDesignScale.current.scale
    return (value * scale).dp
}

/**
 * Int 扩展属性，更简洁的写法
 * 使用：200.sdp
 */
val Int.sdp: Dp
    @Composable get() = designDp(this)

val Float.sdp: Dp
    @Composable get() = designDp(this)
