package com.yikwing.ykquickdev.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.yikwing.ykquickdev.ui.utils.sdp
import androidx.compose.ui.window.DialogProperties

@Composable
fun GradientPage() {
    Column {
        LineGradient()
    }
}

@Composable
fun LineGradient() {
    val gradientColors = listOf(Color(0xFF6A11CB), Color(0xFF2575FC)) // 紫色到蓝色渐变
    val borderRadius = 16.sdp
    val borderWidth = 2.sdp
    val borderColor = Color.Gray // 边框颜色
    Column(
        modifier = Modifier.fillMaxSize().padding(20.sdp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 示例 1: 渐变背景 + 圆角 + 纯色边框
        Box(
            modifier =
                Modifier
                    .size(150.sdp)
                    // 1. 设置边框 (先边框，边框的形状是 RoundedCornerShape)
                    .border(borderWidth, borderColor, RoundedCornerShape(borderRadius))
                    // 2. 设置渐变背景 (后背景，背景的形状也是 RoundedCornerShape，与边框一致)
                    .background(
                        brush =
                            Brush.linearGradient(
                                gradientColors,
                                start = Offset(0f, 0f),
                                end = Offset(0f, Float.POSITIVE_INFINITY),
                            ),
                        shape = RoundedCornerShape(borderRadius),
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Text("Gradient Box 1", color = Color.White)
            Box(
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.sdp, y = (-4).sdp)
                        .size(10.sdp)
                        .background(Color.Red, CircleShape),
            )
        }
        Spacer(modifier = Modifier.height(30.sdp))
        // 示例 2: 渐变边框 + 圆角 + 纯色背景 (相对复杂一点点)
        // 这种情况下，你需要使用一个 Box 来充当边框，内部再放置内容。
        // 或者使用 graphics Layer 绘制，但 for beginners, 嵌套 Box 更直观
        val borderGradient = Brush.linearGradient(listOf(Color.Red, Color.Yellow))
        Box(
            modifier =
                Modifier
                    .size(150.sdp)
                    // 1. 外部 Box 作为渐变边框
                    .background(borderGradient, shape = RoundedCornerShape(borderRadius))
                    // 2. 内部 Box 作为实际内容区域，并带有纯色背景和更小的圆角
                    // 这里 padding 和 innerShapeRadius 决定了边框的粗细和内容区域的形状
                    .padding(borderWidth) // padding 的大小就是边框的宽度
                    .background(
                        Color.DarkGray, // 内部背景色
                        shape = RoundedCornerShape(borderRadius - borderWidth / 2), // 内层圆角要稍微小一点，以避免重叠
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Text("Gradient Border", color = Color.White)
        }

        var showDialog by remember { mutableStateOf(false) }
        // 触发弹窗的按钮
        Button(onClick = { showDialog = true }, modifier = Modifier.padding(16.sdp)) {
            Text("显示自定义弹窗")
        }
        // 弹窗逻辑
        if (showDialog) {
            MyCustomDialogWithNoScrim(
                onDismissRequest = { showDialog = false },
                onConfirm = {
                    // 处理确认逻辑
                    println("Dialog confirmed!")
                    showDialog = false
                },
            )
        }

        Spacer(modifier = Modifier.height(30.sdp))
        Box(
            modifier =
                Modifier
                    .size(300.sdp)
                    .background(
                        Color.LightGray,
                        shape = CircleShape,
                    ).padding(100.sdp)
                    .background(
                        Color.DarkGray,
                        shape = CircleShape,
                    ),
        )
    }
}

@Composable
fun MyCustomDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest, // 点击外部或按返回键触发
        properties =
            DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                // >>>>>>>>>>>>>>>>> 关键在这里: 修改 scrimColor 的透明度 <<<<<<<<<<<<<<<<<
                // 或者使用 MaterialTheme 的颜色，并调整透明度
                // scrimColor = MaterialTheme.colors.onSurface.copy(alpha = 0.4f)
            ),
    ) {
        // 自定义弹窗内容的核心部分
        Column(
            modifier =
                Modifier
                    .fillMaxWidth() // 填充宽度，或指定固定宽度
                    .wrapContentHeight() // 高度自适应内容
                    .clip(RoundedCornerShape(12.sdp)) // 圆角背景
                    .background(Color.LightGray) // 使用主题的背景色
                    .padding(24.sdp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // 标题和关闭按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "我的自定义弹窗",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                )
//                Icon(
//                    imageVector = Icons.Default.Close,
//                    contentDescription = "Close",
//                    modifier =
//                        Modifier
//                            .size(24.dp)
//                            .clickable { onDismissRequest() },
//                    // 点击关闭按钮
//                    tint = Color.Black,
//                )
            }
            Spacer(modifier = Modifier.height(16.sdp))
            // 弹窗说明内容
            Text(
                text = "这是我的自定义弹窗内容。你可以在这里放置任何你想要的 Composable，例如图片、输入框、列表等等。",
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = Color.Black.copy(alpha = 0.7f),
            )
            Spacer(modifier = Modifier.height(24.sdp))
            // 底部按钮区域
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("取消")
                }
                Spacer(modifier = Modifier.width(16.sdp))
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("确认")
                }
            }
        }
    }
}

@Composable
fun MyCustomDialogWithNoScrim(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest, // 点击外部或按返回键触发
        properties =
            DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
//                // 关键属性1: 禁用平台默认宽度，以便我们完全控制大小
//                usePlatformDefaultWidth = false,
//                // 关键属性2: 禁用系统装饰，这样弹窗就可以覆盖全屏了
//                decorFitsSystemWindows = false,
            ),
    ) {
//        Box(
//            modifier =
//                Modifier
//                    .fillMaxSize() // 让这个 Box 铺满整个弹窗区域
//                    // 设置背景为透明，这样你就能看到下面的内容了
//                    .background(Color.Red.copy(alpha = 0.1f))
//                    .clickable(onClick = onDismissRequest) // 允许点击外部透明区域关闭
//                    .wrapContentSize(Alignment.Center), // 让内部内容居中，内容不必再设置 fillMaxWidth
//        ) {
        // 弹窗实际内容
        Column(
            modifier =
                Modifier
                    .wrapContentSize() // 内容自适应大小
                    .clip(RoundedCornerShape(12.sdp))
                    .background(Color.Yellow.copy(alpha = 0.9f)) // 弹窗内容本身可以有颜色和透明度
                    .padding(24.sdp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // ... (弹窗内容) ...
            Text("无蒙层弹窗", Modifier.padding(bottom = 8.sdp), fontWeight = FontWeight.Bold)
            Text("这是一个没有背景蒙层的弹窗，可以看到后面的 LightGray 主页面背景。")
            Spacer(Modifier.height(16.sdp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                Button(onClick = onDismissRequest) { Text("取消") }
                Button(onClick = onConfirm) { Text("确认") }
            }
        }
    }
//    }
}

@Preview
@Composable
private fun LineGradientPreview() {
    LineGradient()
}
