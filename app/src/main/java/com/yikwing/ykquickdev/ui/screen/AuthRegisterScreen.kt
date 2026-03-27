package com.yikwing.ykquickdev.ui.screen

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.yikwing.ykquickdev.components.LocalNavigator
import com.yikwing.ykquickdev.ui.utils.setRoot
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
import com.yikwing.ykquickdev.ui.theme.PermanentMarkerRegular

@Serializable
data object AuthRegister : NavKey

fun EntryProviderScope<NavKey>.authRegisterEntry() {
    entry<AuthRegister> {
        val navigator = LocalNavigator.current
        AuthRegisterScreen(
            navigationToHome = { navigator.setRoot(MainScreen) },
        )
    }
}

@Composable
fun AuthRegisterScreen(navigationToHome: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
        Text(
            "AuthRegister",
            fontFamily = PermanentMarkerRegular,
            fontSize = 24.sp,
            color = Color.White,
            modifier = Modifier.clickable { navigationToHome() },
        )
    }
}