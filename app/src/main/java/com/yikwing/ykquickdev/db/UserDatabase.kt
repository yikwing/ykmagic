package com.yikwing.ykquickdev.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.yikwing.ykquickdev.api.entity.ChapterBean

@Database(
    entities = [User::class, ChapterBean::class],
    version = 1,
    exportSchema = true,
    autoMigrations = []
)
abstract class UserDatabase : RoomDatabase() {

    abstract fun getUserDao(): UserDao
    abstract fun getChapterDao(): ChapterDao
}
