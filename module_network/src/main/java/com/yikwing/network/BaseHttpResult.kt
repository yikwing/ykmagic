package com.yikwing.network

import com.squareup.moshi.JsonClass
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@JsonClass(generateAdapter = true)
data class BaseHttpResult<T>(
    var data: T?,
    var errorMsg: String,
    var errorCode: Int,
)

@Serializable
data class IgnoreHttpResult(
    var data: JsonElement,
    var errorMsg: String,
    var errorCode: Int,
)
