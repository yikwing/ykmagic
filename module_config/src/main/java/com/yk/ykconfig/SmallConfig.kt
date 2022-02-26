package com.yk.ykconfig

import com.yk.ykconfig.annotations.YkConfigNode
import com.yk.ykconfig.annotations.YkConfigValue


@YkConfigNode
data class SmallConfig(
    @YkConfigValue(name = "base_url")
    val baseUrl: String
)
