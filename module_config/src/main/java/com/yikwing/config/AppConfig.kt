package com.yikwing.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppConfig(
    @SerialName("base_url") val baseUrl: String,
)
