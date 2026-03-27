package com.yikwing.ykquickdev.ui.screen

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.yikwing.ykquickdev.components.LocalNavigator
import com.yikwing.ykquickdev.ui.utils.navigate
import kotlinx.serialization.Serializable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.yikwing.ykquickdev.ui.theme.RubikGemstonesRegular

@Serializable
data object DiyInputScreen : NavKey

fun EntryProviderScope<NavKey>.diyInputEntry() {
    entry<DiyInputScreen> {
        val navigator = LocalNavigator.current
        DiyInputScreen(
            navigationToAuth = { navigator.navigate(AuthLogin) },
        )
    }
}

@Composable
fun DiyInputScreen(navigationToAuth: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
        Text(
            "DiyInput",
            fontFamily = RubikGemstonesRegular,
            fontSize = 24.sp,
            color = Color(0xFF002FA7),
            modifier = Modifier.clickable { navigationToAuth() },
        )
    }
}
