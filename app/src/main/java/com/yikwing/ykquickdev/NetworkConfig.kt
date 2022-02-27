package com.yikwing.ykquickdev

import com.squareup.moshi.JsonClass
import com.yk.ykconfig.annotations.YkConfigNode
import com.yk.ykconfig.annotations.YkConfigValue


@YkConfigNode
@JsonClass(generateAdapter = true)
data class NetworkConfig(
    @YkConfigValue(name = "base_url")
    val baseUrl: String
)
