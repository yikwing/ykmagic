package com.yikwing.ykquickdev.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.room3.Room
import com.yikwing.ykquickdev.UserPreferences
import com.yikwing.ykquickdev.db.ChapterDao
import com.yikwing.ykquickdev.db.UserDao
import com.yikwing.ykquickdev.db.UserDatabase
import com.yikwing.ykquickdev.datastore.userPreferencesStore
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
@Configuration
object DataModule {
    @Singleton
    fun provideDataBase(context: Context): UserDatabase =
        Room
            .databaseBuilder(
                context.applicationContext,
                UserDatabase::class.java,
                "Users.db",
            ).build()

    @Singleton
    fun provideUserDao(userDatabase: UserDatabase): UserDao = userDatabase.getUserDao()

    @Singleton
    fun provideChapterDao(userDatabase: UserDatabase): ChapterDao = userDatabase.getChapterDao()

    @Singleton
    fun provideUserPreferencesDataStore(context: Context): DataStore<UserPreferences> = context.userPreferencesStore
}
