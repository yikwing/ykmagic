package com.yikwing.ykquickdev.api.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@JsonClass(generateAdapter = true)
data class HttpBinHeaders(
    val headers: Headers,
)

@Serializable
@JsonClass(generateAdapter = true)
data class Headers(
    @SerialName("Host") @Json(name = "Host") val host: String,
    @SerialName("User-Agent") @Json(name = "User-Agent") val userAgent: String,
    @SerialName("X-Amzn-Trace-Id") @Json(name = "X-Amzn-Trace-Id") val traceId: String,
)
