package com.yikwing.network

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class BaseHttpResult<T>(
    val data: T?,
    val errorMsg: String,
    val errorCode: Int,
)

@Serializable
data class IgnoreHttpResult(
    val data: JsonElement,
    val errorMsg: String,
    val errorCode: Int,
)
