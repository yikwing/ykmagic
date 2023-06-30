package com.yikwing.ykquickdev.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yikwing.ykquickdev.ui.screen.OtherPageScreen
import com.yikwing.ykquickdev.ui.screen.PackageInfoScreen
import kotlinx.coroutines.CoroutineScope

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    startDestination: String = PackageInfoScreen.route,
    navActions: AppNavigationActions = remember(navController) {
        AppNavigationActions(navController)
    }
) {
    val currentNavBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentNavBackStackEntry?.destination?.route ?: startDestination

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(PackageInfoScreen.route) {
            PackageInfoScreen(navigationToPage = navActions::navigationToA)
        }
        composable(
            route = OtherPageScreen.routeWithArgs,
            arguments = OtherPageScreen.argument
        ) { navBackStackEntry ->
            val accountType = navBackStackEntry.arguments?.getString(OtherPageScreen.paramTypeArg)
            OtherPageScreen(accountType)
        }
    }
}
