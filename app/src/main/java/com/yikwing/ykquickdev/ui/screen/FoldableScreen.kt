package com.yikwing.ykquickdev.ui.screen

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.window.layout.FoldingFeature
import com.yikwing.extension.device.FoldPosture
import com.yikwing.extension.device.foldingFeatureFlow
import com.yikwing.extension.device.hasHingeSensor
import com.yikwing.extension.device.posture
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.Serializable

@Serializable
data object FoldableRoute : NavKey

fun EntryProviderScope<NavKey>.foldableEntry() {
    entry<FoldableRoute> { FoldableScreen() }
}

@Composable
fun FoldableScreen(modifier: Modifier = Modifier) {
    val activity = LocalActivity.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 有 Activity 时订阅实时折叠状态；预览/非 Activity 语境下退化为空流。
    val foldingFlow = remember(activity) { activity?.foldingFeatureFlow() ?: flowOf(null) }
    val feature: FoldingFeature? by foldingFlow.collectAsStateWithLifecycle(
        initialValue = null,
        lifecycleOwner = lifecycleOwner,
    )

    val hasHinge = activity?.hasHingeSensor() == true
    val posture = feature.posture()

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            "当前姿态: " +
                when (posture) {
                    FoldPosture.NORMAL -> "普通屏幕"
                    FoldPosture.OPENED -> "折叠屏已展开"
                    FoldPosture.HALF -> "折叠屏半折叠"
                },
        )
        Text("铰链传感器: $hasHinge")
        Text("FoldingFeature.state: ${feature?.state ?: "-"}")
        Text("FoldingFeature.orientation: ${feature?.orientation ?: "-"}")
    }
}
