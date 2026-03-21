package com.yikwing.ykquickdev.di

import com.yikwing.network.NetworkModule
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module

@Module(
    includes = [
        AppFeatureModule::class,
        NetworkModule::class,
    ],
)
@Configuration
object AppModule
