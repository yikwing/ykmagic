package com.yikwing.config

import com.squareup.moshi.JsonClass
import com.yikwing.config.annotations.YkConfigNode
import com.yikwing.config.annotations.YkConfigValue

@JsonClass(generateAdapter = true)
@YkConfigNode
data class SmallConfig(
    @YkConfigValue(path = "test_string")
    val testString: String,
    @YkConfigValue(path = "test_other.test_boolean")
    val testBoolean: Boolean,
    @YkConfigValue(path = "test_other.test_int")
    val testInt: Int,
    @YkConfigValue(path = "test_other.test_double")
    val testDouble: Double,
)
