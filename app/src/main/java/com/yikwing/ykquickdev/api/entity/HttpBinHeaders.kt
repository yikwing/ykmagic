package com.yikwing.ykquickdev.api.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class HttpBinHeaders(
    val headers: Headers,
) : Parcelable

@Serializable
@Parcelize
data class Headers(
    @property:SerialName("Host") val host: String,
    @property:SerialName("User-Agent") val userAgent: String,
    @property:SerialName("X-Amzn-Trace-Id") val traceId: String,
) : Parcelable
