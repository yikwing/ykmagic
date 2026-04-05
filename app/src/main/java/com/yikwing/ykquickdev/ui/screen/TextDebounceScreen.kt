package com.yikwing.ykquickdev.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.yikwing.compose.interaction.rememberDebounceClick
import com.yikwing.ykquickdev.components.LocalNavigator
import com.yikwing.ykquickdev.ui.theme.RubikGemstonesRegular
import com.yikwing.ykquickdev.ui.utils.navigate
import com.yikwing.ykquickdev.ui.utils.sdp
import kotlinx.serialization.Serializable

@Serializable
data object TextDebounce : NavKey

fun EntryProviderScope<NavKey>.textDebounceEntry() {
    entry<TextDebounce> {
        val navigator = LocalNavigator.current
        TextDebounceScreen(
            navigationToPackInfo = dropUnlessResumed { navigator.navigate(PackageInfoScreen) },
        )
    }
}

@Composable
fun TextDebounceScreen(navigationToPackInfo: () -> Unit) {
    var count by remember { mutableIntStateOf(0) }

    LaunchedEffect(count) {
        if (count == 3) {
            navigationToPackInfo()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(100.sdp)
                    .background(Color.Cyan),
        )

        Text(
            "$count",
            fontFamily = RubikGemstonesRegular,
            color = Color.Black,
            fontSize = 24.sp,
            modifier =
                Modifier
                    .statusBarsPadding()
                    .padding(16.sdp),
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                Modifier.align(
                    Alignment.Center,
                ),
        ) {
            Button(onClick = rememberDebounceClick { count++ }) {
                Text("Plus")
            }

            Text(
                "$count",
                fontFamily = RubikGemstonesRegular,
                color = Color.White,
                fontSize = 24.sp,
            )

            Button(onClick = rememberDebounceClick { count-- }) {
                Text("Minus")
            }
        }
    }
}
