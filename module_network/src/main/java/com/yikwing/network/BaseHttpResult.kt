package com.yikwing.network

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BaseHttpResult<T>(
    var data: T?,
    var errorMsg: String,
    var errorCode: Int,
)
