package com.yk.ykconfig

import com.squareup.moshi.JsonClass
import com.yk.ykconfig.annotations.YkConfigNode
import com.yk.ykconfig.annotations.YkConfigValue


@JsonClass(generateAdapter = true)
@YkConfigNode
data class SmallConfig(
    @YkConfigValue(name = "test_string")
    val testString: String,
    @YkConfigValue(name = "test_other.test_boolean")
    val testBoolean: Boolean,
    @YkConfigValue(name = "test_other.test_int")
    val testInt: Int,
    @YkConfigValue(name = "test_other.test_double")
    val testDouble: Double
)
