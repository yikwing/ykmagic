package com.yikwing.ykquickdev.api.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "t_chapter")
@JsonClass(generateAdapter = true)
data class ChapterBean(
    @ColumnInfo(name = "course_id") val courseId: Int,
    @PrimaryKey val id: Int,
    val name: String,
    val order: Int,
    @ColumnInfo(name = "parent_chapter_id") val parentChapterId: Int,
    @ColumnInfo(name = "user_control_set_top") val userControlSetTop: Boolean,
    val visible: Int,
)
