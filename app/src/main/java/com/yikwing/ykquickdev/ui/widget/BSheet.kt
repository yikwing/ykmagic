package com.example.jetpackcompose.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yikwing.ykquickdev.ui.utils.sdp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyMaterial3ModalBottomSheetExample() {
    // 控制 Bottom Sheet 的显示与隐藏
    var showBottomSheet by remember { mutableStateOf(false) }
    // M3 的 SheetState, 同样可以控制 HalfExpanded
    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true, // true 会跳过半展开状态
        )
    val scope = rememberCoroutineScope()
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.sdp)
                .background(Color.LightGray),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        Text("Material 3 底部工作表示例", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(32.sdp))
        Button(onClick = { showBottomSheet = true }) {
            Text("显示 Bottom Sheet")
        }
    }
    // 当 showBottomSheet 为 true 时，显示 ModalBottomSheet
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                // 当用户点击蒙层或向下滑动 Bottom Sheet 时触发
                showBottomSheet = false
            },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface, // M3 主题的表面色
            contentColor = MaterialTheme.colorScheme.onSurface, // M3 主题的表面内容色
            shape =
                RoundedCornerShape(
                    topStart = 16.sdp,
                    topEnd = 16.sdp,
                    bottomEnd = 0.0.dp,
                    bottomStart = 0.0.dp,
                ),
            // 例如：RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            // 你提供的 dragHandle 自定义示例
            dragHandle = {
                // 自定义拖动指示器。默认情况下 M3 会提供一个灰色的短条。
                // 如果你想完全移除，可以设置 {} 为空 lambda。
                // 这里我们显示一个自定义的拖动指示器
                CustomDragHandle(
                    // 根据实际需求传递参数
                    showDragIndicator = false,
                    indicatorColor = Color(0xFF54CEE3),
                )
            },
            // 通常不需要指定 scrimColor，M3 默认会提供一个合适的蒙层
        ) {
            // 这是你的 Bottom Sheet 的内容区域
            BottomSheetContentM3(
                onCloseClicked = {
                    scope.launch {
                        sheetState.hide()
                        showBottomSheet = false
                    }
                },
            )
        }
    }
}

// ======================= 自定义拖动指示器 Composable =======================
@Composable
fun CustomDragHandle(
    showDragIndicator: Boolean,
    indicatorColor: Color,
) {
    if (showDragIndicator) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.sdp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(40.sdp, 4.sdp)
                        .clip(CircleShape)
                        .background(indicatorColor),
            )
        }
    }
}

// ======================= Bottom Sheet 内容 Composable (for M3) =======================
@Composable
fun BottomSheetContentM3(onCloseClicked: () -> Unit) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 200.sdp, max = 500.sdp) // 限定 Bottom Sheet 的最小/最大高度
                .padding(16.sdp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.sdp),
    ) {
        Text("🚀 Material 3 底部工作表标题", style = MaterialTheme.typography.titleMedium)
        Text("体验更简洁的 API 和 Material Design 3 风格。")
        Text("您可以使用 LazyColumn 或其他滚动组件来显示长列表。")
        Spacer(modifier = Modifier.height(16.sdp))
        Button(onClick = onCloseClicked) {
            Text("关闭 Bottom Sheet")
        }
        // 模拟一个可以拖动的长内容
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
            // 填充剩余空间并可滚动
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            items(10) {
                Text("这是一个长内容的第 ${it + 1} 项。")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewMyMaterial3ModalBottomSheetExample() {
    MaterialTheme {
        // 使用 Material 3 的主题
        MyMaterial3ModalBottomSheetExample()
    }
}
