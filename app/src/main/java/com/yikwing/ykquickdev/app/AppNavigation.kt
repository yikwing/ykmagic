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
}
