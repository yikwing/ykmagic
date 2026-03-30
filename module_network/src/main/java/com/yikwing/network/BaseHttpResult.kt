package com.yikwing.network

import kotlinx.serialization.Serializable

@Serializable
data class BaseHttpResult<T>(
    val data: T?,
    val errorMsg: String,
    val errorCode: Int,
)
