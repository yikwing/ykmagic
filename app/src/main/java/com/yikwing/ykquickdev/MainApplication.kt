package com.yikwing.ykquickdev

import android.app.Application
import android.net.Uri
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.util.Logger
import com.yikwing.extension.NetConnectManager
import com.yikwing.extension.copyAssetToCache
import com.yikwing.extension.image.compressImageFromUri
import com.yikwing.network.checkProxy
import com.yikwing.proxy.startup.AppInitializer
import com.yikwing.ykquickdev.task.ConfigInjectInitTask
import com.yikwing.ykquickdev.task.DataStoreInitTask
import com.yikwing.ykquickdev.task.LoggerInitTask
import com.yikwing.ykquickdev.task.NetworkInitTask
import dagger.hilt.android.HiltAndroidApp
import java.io.File
import kotlin.system.measureTimeMillis

@HiltAndroidApp
class MainApplication :
    Application(),
    ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()

        Log.i("checkProxy", checkProxy().toString())

        featureTest()

        initSetup()

        NetConnectManager.init(this)
    }

    private fun featureTest() {
        testCompress()
    }

    private fun testCompress() {
        val fileName = "beautiful-girl-8080757.jpg"
        val copiedFile = copyAssetToCache(this, fileName)

        if (copiedFile != null) {
            Log.i("CopyAsset", "文件已成功复制到 cache 目录: ${copiedFile.absolutePath}")

            compressImageFromUri(
                this,
                Uri.fromFile(copiedFile),
                File(this.cacheDir, "cache_uri_$fileName").path,
                100,
            )
        } else {
            Log.e("CopyAsset", "文件复制失败")
        }
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

    override fun newImageLoader(): ImageLoader =
        ImageLoader
            .Builder(this)
            .crossfade(true)
            .logger(
                object : Logger {
                    override var level: Int = Log.DEBUG

                    override fun log(
                        tag: String,
                        priority: Int,
                        message: String?,
                        throwable: Throwable?,
                    ) {
                        Log.i(tag, message.toString())
                    }
                },
            ).build()
}
