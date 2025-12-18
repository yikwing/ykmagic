package com.yikwing.ykquickdev.ui.widget

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner

private fun currentOnStart() {
    Log.i("UIRoot", "currentOnStart")
}

private fun currentOnStop() {
    Log.i("UIRoot", "currentOnStop")
}

@Composable
fun UIRoot(
    navigationToGradient: () -> Unit,
    navigationToBSheet: () -> Unit,
    navigationToConstraint: () -> Unit,
    modifier: Modifier = Modifier, // 修饰符参数始终赋默认值
) {
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer =
            object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    super.onStart(owner)
                    currentOnStart()
                }

                override fun onStop(owner: LifecycleOwner) {
                    super.onStop(owner)
                    currentOnStop()
                }
            }

        // Add the observer to the lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)

        // When the effect leaves the Composition, remove the observer
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        // 将通用屏幕修饰符放在这里，确保内部元素有稳定布局
        modifier =
            Modifier
                .fillMaxSize() // 让 Column 填充整个可用空间
                .padding(16.dp) // 给整个 Column 添加外部内边距
                .then(modifier),
        // 将外部传入的 modifier 应用在此之后
        verticalArrangement = Arrangement.spacedBy(8.dp), // 按钮之间自动添加间隔
        horizontalAlignment = Alignment.CenterHorizontally, // 让按钮居中
    ) {
        // 由于 Column 已经有了 padding 和 verticalArrangement，
        // 按钮内部就不需要重复设置 horizontal = 16.dp 和 Spacer 了
        Button(
            // 这里只需要 fillMaxWidth()
            modifier = Modifier.fillMaxWidth(),
            onClick = navigationToGradient,
        ) {
            Text(text = "渐变")
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = navigationToBSheet,
        ) {
            Text(text = "ModalBottomSheet")
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = navigationToConstraint,
        ) {
            Text(text = "Constraint")
        }
    }
}

@Preview
@Composable
private fun UIRootPreView() {
    UIRoot(navigationToGradient = {}, navigationToBSheet = {}, navigationToConstraint = {})
}
