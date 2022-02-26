package com.yk.yknetwork

data class BaseHttpResult<T>(
    var data: T?,
    var errorMsg: String,
    var errorCode: Int
)