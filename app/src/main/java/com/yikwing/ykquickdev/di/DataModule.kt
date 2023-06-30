package com.yikwing.ykquickdev.di

import android.content.Context
import androidx.room.Room
import com.yikwing.ykquickdev.db.ChapterDao
import com.yikwing.ykquickdev.db.UserDao
import com.yikwing.ykquickdev.db.UserDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDataBase(
        @ApplicationContext context: Context
    ): UserDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            UserDatabase::class.java,
            "Users.db"
        ).build()
    }

    @Provides
    fun provideUserDao(
        userDatabase: UserDatabase
    ): UserDao = userDatabase.getUserDao()

    @Provides
    fun provideChapterDao(
        userDatabase: UserDatabase
    ): ChapterDao = userDatabase.getChapterDao()
}
