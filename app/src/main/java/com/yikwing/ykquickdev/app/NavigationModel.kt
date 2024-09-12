package com.yikwing.ykquickdev.app

import kotlinx.serialization.Serializable

@Serializable
data object PackageInfoScreen

@Serializable
data class Product(
    val id: String,
)

@Serializable
data object DiyInputScreen
