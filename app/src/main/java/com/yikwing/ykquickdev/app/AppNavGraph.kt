package com.yikwing.ykquickdev.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    startDestination: Any = MainScreen,
    navActions: AppNavigationActions =
        remember(navController) {
            AppNavigationActions(navController)
        },
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        tabNavGraph(modifier, navActions)
        mainScreens(modifier, navActions)
        authNavGraph(navActions)
        uiNavGraph(modifier, navActions)
    }
}
