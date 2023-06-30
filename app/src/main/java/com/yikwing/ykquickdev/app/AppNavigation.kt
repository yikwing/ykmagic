package com.yikwing.ykquickdev.app

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument

interface AppDestination {
    val route: String
}

object PackageInfoScreen : AppDestination {
    override val route: String = "packageInfo"
}

object OtherPageScreen : AppDestination {
    override val route: String = "otherPage"

    const val paramTypeArg = "param_type"
    val routeWithArgs = "$route/{$paramTypeArg}"

    val argument = listOf(
        navArgument(paramTypeArg) {
            type = NavType.StringType
        }
    )
}

class AppNavigationActions(private val navController: NavHostController) {
    fun navigationToA(msg: String = "default") {
        navController.navigateToSingleAccount(msg)
    }
}

fun NavHostController.navigateSingleTopTo(route: String) =
    this.navigate(route) {
        // Pop up to the start destination of the graph to
        // avoid building up a large stack of destinations
        // on the back stack as users select items
        popUpTo(
            this@navigateSingleTopTo.graph.findStartDestination().id
        ) {
            saveState = true
        }
        // Avoid multiple copies of the same destination when
        // reselecting the same item
        launchSingleTop = true
        // Restore state when reselecting a previously selected item
        restoreState = true
    }

private fun NavHostController.navigateToSingleAccount(accountType: String) {
    this.navigateSingleTopTo("${OtherPageScreen.route}/$accountType")
}
