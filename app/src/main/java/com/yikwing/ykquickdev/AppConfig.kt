package com.yikwing.ykquickdev

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import com.yk.ykconfig.annotations.YkConfigNode
import com.yk.ykconfig.annotations.YkConfigValue

@YkConfigNode
@JsonClass(generateAdapter = true)
@Keep
data class NetworkConfig(
    @YkConfigValue(path = "base_url") val baseUrl: String,
)
