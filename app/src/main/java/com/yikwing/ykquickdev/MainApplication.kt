package com.yikwing.ykquickdev

import android.app.Application
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.yikwing.network.checkProxy
import com.yikwing.proxy.startup.AppInitializer
import com.yikwing.ykquickdev.task.ConfigInjectInitTask
import com.yikwing.ykquickdev.task.DataStoreInitTask
import com.yikwing.ykquickdev.task.LoggerInitTask
import com.yikwing.ykquickdev.task.NetworkInitTask
import dagger.hilt.android.HiltAndroidApp
import kotlin.system.measureTimeMillis

@HiltAndroidApp
class MainApplication :
    Application(),
    ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()

        Log.i("checkProxy", checkProxy().toString())

        initSetup()
    }

    private fun initSetup() {
        val spendTime =
            measureTimeMillis {
                // 初始化SDK或者执行一些操作
                AppInitializer
                    .getInstance(this)
                    .addTask(ConfigInjectInitTask())
                    .addTask(LoggerInitTask())
                    .addTask(NetworkInitTask())
                    .addTask(DataStoreInitTask())
                    .build(debug = true)
            }
        Log.i("initSetup", "spendTime: $spendTime")
    }

    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this).crossfade(true).build()
}
