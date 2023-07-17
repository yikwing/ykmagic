package com.yikwing.ykquickdev

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import com.yikwing.config.annotations.YkConfigNode
import com.yikwing.config.annotations.YkConfigValue

@YkConfigNode
@JsonClass(generateAdapter = true)
@Keep
data class NetworkConfig(
    @YkConfigValue(path = "base_url") val baseUrl: String,
)
