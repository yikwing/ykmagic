package com.yikwing.ykquickdev.api.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HttpBinHeaders(
    val headers: Headers,
)

@Serializable
data class Headers(
    @property:SerialName("Host") val host: String,
    @property:SerialName("User-Agent") val userAgent: String,
    @property:SerialName("X-Amzn-Trace-Id") val traceId: String,
)
