package com.yikwing.ykquickdev.app

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object MainScreen : NavKey

@Serializable
data object PackageInfoScreen : NavKey

@Serializable
data object TextDebounce : NavKey

@Serializable
data class Product(
    val id: String,
) : NavKey

@Serializable
data object DiyInputScreen : NavKey

sealed interface TabDestination : NavKey {
    @Serializable
    data object TabNavigation : TabDestination

    @Serializable
    data object Home : TabDestination

    @Serializable
    data object Mine : TabDestination
}

sealed interface AuthDestination : NavKey {
    @Serializable
    data object AuthNavigation : AuthDestination

    // 登录页
    @Serializable
    data object Login : AuthDestination

    // 注册页
    @Serializable
    data object Register : AuthDestination
}

sealed interface UIDestination : NavKey {
    @Serializable
    data object UINavigation : UIDestination

    @Serializable
    data object UIRoot : UIDestination

    @Serializable
    data object Gradient : UIDestination

    @Serializable
    data object BSheet : UIDestination

    @Serializable
    data object ConstraintPage : UIDestination
}
