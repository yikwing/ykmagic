package com.yikwing.ykquickdev.api.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HttpBinHeaders(
    val headers: Headers,
)

@JsonClass(generateAdapter = true)
data class Headers(
    @property:Json(name = "Host") val host: String,
    @property:Json(name = "User-Agent") val userAgent: String,
    @property:Json(name = "X-Amzn-Trace-Id") val traceId: String,
)
