package com.yikwing.config

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Person(
    val name: String,
    val age: Int,
    val sex: Sex
)

enum class Sex {
    MALE,
    FEMALE,
    NORMAL
}
