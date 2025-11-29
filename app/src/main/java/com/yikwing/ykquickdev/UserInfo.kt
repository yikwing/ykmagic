package com.yikwing.ykquickdev

import com.yikwing.ykquickdev.api.serializers.BooleanAsIntSerializer
import kotlinx.serialization.Serializable

@Serializable
data class UserInfo(
    val name: String,
    val age: Int,
    @Serializable(with = BooleanAsIntSerializer::class) val isMan: Boolean,
)
