package com.yikwing.ykquickdev.api.entity

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class HttpBinPostResult(
    val json: JsonElement,
    val url: String,
)
