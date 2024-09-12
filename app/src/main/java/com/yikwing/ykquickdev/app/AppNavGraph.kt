package com.yikwing.ykquickdev.app

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.yikwing.ykquickdev.ui.screen.DiyInputWrapperScreen
import com.yikwing.ykquickdev.ui.screen.OtherPageScreen
import com.yikwing.ykquickdev.ui.screen.PackageInfoScreen
import kotlinx.coroutines.CoroutineScope

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    startDestination: Any = PackageInfoScreen,
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
        composable<PackageInfoScreen> {
            PackageInfoScreen(
                navigationToPage = navActions::navigationToA,
                navigationToDiy = navActions::navigationToDiyInput,
            )
        }

        composable<Product>(
            deepLinks =
                listOf(
                    navDeepLink<Product>(
                        basePath = "www.wing.com/product",
                    ),
                ),
        ) { backStackEntry ->

            Log.d("AppNavGraph", backStackEntry.destination.route ?: "")

            val product: Product = backStackEntry.toRoute()
            OtherPageScreen(product.id)
        }

        composable<DiyInputScreen> {
            DiyInputWrapperScreen()
        }
    }
}
