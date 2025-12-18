package com.yikwing.ykquickdev.app

import androidx.navigation.NavHostController

class AppNavigationActions(
    private val navController: NavHostController,
) {
    fun navigationToA(msg: String = "default") {
        navController.navigate(route = Product(id = msg))
    }

    fun navigationToDiyInput() {
        navController.navigate(route = DiyInputScreen)
    }

    fun navigationToUI() {
        navController.navigate(route = UIDestination.UINavigation)
    }

    fun navigationToGradient() {
        navController.navigate(route = UIDestination.Gradient)
    }

    fun navigationToBSheet() {
        navController.navigate(route = UIDestination.BSheet)
    }

    fun navigationToConstraint() {
        navController.navigate(route = UIDestination.ConstraintPage)
    }

    fun navigationToAuth() {
        navController.navigate(route = AuthDestination.AuthNavigation)
    }

    fun navigationToRegister() {
        navController.navigate(route = AuthDestination.Register)
    }

    fun navigationToHome() {
        navController.navigate(route = PackageInfoScreen) {
            // 弹出到 Auth 模块的根路由，并包含它本身
            popUpTo(PackageInfoScreen) {
                inclusive = false
            }
            launchSingleTop = true
        }
    }
}
