package com.yikwing.ykquickdev.api.entity

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
@Parcelize
data class HttpBinHeaders(
    val headers: Headers,
) : Parcelable

@Immutable
@Serializable
@Parcelize
data class Headers(
    @property:SerialName("Host") val host: String,
    @property:SerialName("User-Agent") val userAgent: String,
    @property:SerialName("X-Amzn-Trace-Id") val traceId: String,
) : Parcelable
