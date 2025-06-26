package com.yikwing.ykquickdev.api.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HttpBinHeaders(
    val headers: Headers,
)

@JsonClass(generateAdapter = true)
data class Headers(
    @field:Json(name = "Host") val host: String,
    @field:Json(name = "User-Agent") val userAgent: String,
    @field:Json(name = "X-Amzn-Trace-Id") val traceId: String,
)
