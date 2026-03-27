package com.yikwing.ykquickdev.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.yikwing.ykquickdev.components.LocalNavigator
import com.yikwing.ykquickdev.ui.theme.PermanentMarkerRegular
import com.yikwing.ykquickdev.ui.utils.navigate
import com.yikwing.ykquickdev.viewmodel.AuthLoginViewModel
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data object AuthLogin : NavKey

fun EntryProviderScope<NavKey>.authLoginEntry() {
    entry<AuthLogin> {
        val navigator = LocalNavigator.current
        AuthLoginScreen(
            navigationToRegister = { navigator.navigate(AuthRegister) },
        )
    }
}

@Composable
fun AuthLoginScreen(
    navigationToRegister: () -> Unit,
    viewModel: AuthLoginViewModel = koinViewModel(),
) {
    val name by viewModel.name.collectAsStateWithLifecycle()

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
        Text(
            name,
            fontFamily = PermanentMarkerRegular,
            fontSize = 24.sp,
            color = Color.White,
            modifier =
                Modifier.clickable {
                    viewModel.changeName("changed")
                    navigationToRegister()
                },
        )
    }
}
