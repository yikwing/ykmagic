package com.yikwing.ykquickdev.app

import kotlinx.serialization.Serializable

sealed interface AppDestination

@Serializable
data object PackageInfoScreen : AppDestination

@Serializable
data class Product(
    val id: String,
) : AppDestination

@Serializable
data object DiyInputScreen : AppDestination

sealed interface TabDestination : AppDestination {
    @Serializable
    data object TabNavigation : TabDestination

    @Serializable
    data object Home : TabDestination

    @Serializable
    data object Mine : TabDestination
}

sealed interface AuthDestination : AppDestination {
    @Serializable
    data object AuthNavigation : AuthDestination

    // 登录页
    @Serializable
    data object Login : AuthDestination

    // 注册页
    @Serializable
    data object Register : AuthDestination
}

sealed interface UIDestination : AppDestination {
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
