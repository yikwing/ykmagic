package com.yikwing.network

import com.squareup.moshi.JsonClass
import kotlinx.serialization.Serializable

@Serializable
@JsonClass(generateAdapter = true)
data class BaseHttpResult<T>(
    var data: T?,
    var errorMsg: String,
    var errorCode: Int,
)
