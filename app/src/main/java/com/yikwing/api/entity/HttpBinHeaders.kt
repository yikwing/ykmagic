package com.yikwing.api.entity

import com.google.gson.annotations.SerializedName

data class HttpBinHeaders(
    val headers: Headers
)

data class Headers(
    @SerializedName(value = "host")
    val host: String,
    @SerializedName(value = "User-Agent")
    val userAgent: String,
    @SerializedName(value = "X-Amzn-Trace-Id")
    val traceId: String,
)