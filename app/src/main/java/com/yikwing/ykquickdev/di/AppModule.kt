package com.yikwing.ykquickdev.di

import com.yikwing.network.di.NetworkScanModule
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module

@Module(
    includes = [
        AppFeatureModule::class,
        NetworkScanModule::class,
    ],
)
@Configuration
object AppModule
