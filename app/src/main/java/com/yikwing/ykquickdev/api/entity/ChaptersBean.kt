package com.yikwing.ykquickdev.api.entity

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChaptersBean(
    val courseId: Int,
    val id: Int,
    val name: String,
    val order: Int,
    val parentChapterId: Int,
    val userControlSetTop: Boolean,
    val visible: Int
)