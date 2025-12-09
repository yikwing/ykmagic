package com.yikwing.ykquickdev.di

import android.content.Context
import androidx.room.Room
import com.yikwing.ykquickdev.db.ChapterDao
import com.yikwing.ykquickdev.db.UserDao
import com.yikwing.ykquickdev.db.UserDatabase
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import javax.inject.Singleton

@Module
object DataModule {
    @Singleton
    fun provideDataBase(context: Context): UserDatabase =
        Room
            .databaseBuilder(
                context.applicationContext,
                UserDatabase::class.java,
                "Users.db",
            ).build()

    @Factory
    fun provideUserDao(userDatabase: UserDatabase): UserDao = userDatabase.getUserDao()

    @Factory
    fun provideChapterDao(userDatabase: UserDatabase): ChapterDao = userDatabase.getChapterDao()
}
