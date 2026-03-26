package com.yikwing.ykquickdev.ui.screen

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.yikwing.ykquickdev.ui.utils.navigate
import kotlinx.serialization.Serializable
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yikwing.ykquickdev.components.VerificationCodeTextField
import com.yikwing.ykquickdev.ui.theme.RubikGemstonesRegular

@Serializable
data object DiyInputScreen : NavKey

fun EntryProviderScope<NavKey>.diyInputEntry(backStack: NavBackStack<NavKey>) {
    entry<DiyInputScreen> {
        DiyInputScreen(
            navigationToAuth = { backStack.navigate(AuthLogin) },
        )
    }
}

@Composable
fun DiyInputWrapperScreen() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column {
            Spacer(modifier = Modifier.height(60.dp))
            VerificationCodeTextField {
                Log.d("DiyInputScreen", "===== $it")
            }
        }
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
