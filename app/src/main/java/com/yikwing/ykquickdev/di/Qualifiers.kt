package com.yikwing.ykquickdev.di

import org.koin.core.annotation.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UserPreferencesStore

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppSettingsStore
