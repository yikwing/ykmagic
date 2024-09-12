package com.yikwing.ykquickdev.di

import com.squareup.moshi.Moshi
import com.squareup.wire.WireJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Singleton
    @Provides
    fun moshi(): Moshi = Moshi.Builder().add(WireJsonAdapterFactory()).build()
}
