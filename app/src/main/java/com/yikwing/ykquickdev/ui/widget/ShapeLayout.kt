package com.yikwing.ykquickdev.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yikwing.ykquickdev.ui.screen.permanentMarkerRegular

/**
 * <pre>
 *     author: yikwing
 *
 *        _ _              _
 *       (_) |            (_)
 *  _   _ _| | ____      ___ _ __   __ _
 * | | | | | |/ /\ \ /\ / / | '_ \ / _` |
 * | |_| | |   <  \ V  V /| | | | | (_| |
 *  \__, |_|_|\_\  \_/\_/ |_|_| |_|\__, |
 *   __/ |                          __/ |
 *  |___/                          |___/
 *
 *     email : 49999@live.com
 *     time  : 2025-12-21 21:39
 *     desc  :
 * </pre>
 */

private val shape = RoundedCornerShape(22.dp)

private val borderBrush =
    Brush.linearGradient(
        colors =
            listOf(
                Color(0xFF00FF87), // 霓虹绿
                Color(0xFF60EFFF), // 青蓝
            ),
    )

private val bgBrush =
    Brush.linearGradient(
        colors =
            listOf(
                Color(0xFF667EEA),
                Color(0xFFEC4899),
            ),
    )

@Composable
fun ShapeButton(
    text: String,
    click: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            Modifier
                .then(modifier)
                .clip(shape)
                .clickable {
                    click()
                }.border(
                    width = 1.dp,
                    brush = borderBrush,
                    shape = shape,
                ).background(
                    brush = bgBrush,
                ),
    ) {
        Text(
            text,
            textAlign = TextAlign.Center,
            style =
                TextStyle(
                    color = Color.White,
                    fontSize = 20.sp,
                    fontFamily = permanentMarkerRegular,
                    // 移除字体额外 padding，使文字垂直居中更精确
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                ),
            modifier = Modifier.padding(horizontal = 12.dp),
        )
    }
}

@Preview
@Composable
fun ShapeButtonPreview() {
    ShapeButton(
        "Hello",
        modifier =
            Modifier
                .wrapContentWidth()
                .height(44.dp),
    )
}
