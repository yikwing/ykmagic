package com.yikwing.ykquickdev

import android.app.Activity
import android.app.Application
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.request.crossfade
import coil3.util.Logger
import com.yikwing.extension.NetConnectManager
import com.yikwing.extension.asColor
import com.yikwing.extension.copyAssetToCache
import com.yikwing.extension.image.compressImageFromUri
import com.yikwing.extension.plus.noOpDelegate
import com.yikwing.network.checkProxy
import com.yikwing.proxy.startup.AppInitializer
import com.yikwing.proxy.util.ActivityHierarchyManager
import com.yikwing.ykquickdev.task.ConfigInjectInitTask
import com.yikwing.ykquickdev.task.DataStoreInitTask
import com.yikwing.ykquickdev.task.LoggerInitTask
import com.yikwing.ykquickdev.task.NetworkInitTask
import com.yikwing.ykquickdev.work.CleanCacheWork
import io.kotzilla.sdk.analytics.koin.analytics
import org.koin.android.ext.koin.androidContext
import org.koin.core.annotation.KoinApplication
import org.koin.ksp.generated.startKoin
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

@KoinApplication
class MainApplication :
    Application(),
    SingletonImageLoader.Factory {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MainApplication)
            analytics {
                onConfig {
                    refreshRate = 15_000L // Send metrics every 15 seconds
                    useDebugLogs = true
                }
            }
        }

        R.color.black.asColor()

        Log.i("checkProxy", checkProxy().toString())

        registerActivityLifecycleCallbacks(AppActivityLifecycleCallbacks())

        featureTest()

        initSetup()

        NetConnectManager.init(this)

        scheduleCacheCleanup()
    }

    private fun scheduleCacheCleanup() {
        val constraints =
            Constraints
                .Builder()
                .setRequiresBatteryNotLow(true) // 仅在电量充足时执行
                .setRequiresCharging(true) // 仅在充电时执行，避免 setRequiresDeviceIdle 限制任务运行
                .build()

        val workRequest =
            PeriodicWorkRequestBuilder<CleanCacheWork>(
                1,
                TimeUnit.DAYS, // 每天执行一次
            ).setConstraints(constraints).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "clear_cache_worker",
            ExistingPeriodicWorkPolicy.UPDATE, // 确保 Work 任务更新
            workRequest,
        )

        // 一次执行
        val workRequestOnce = OneTimeWorkRequestBuilder<CleanCacheWork>().build()
        WorkManager.getInstance(this).enqueue(workRequestOnce)
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

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader
            .Builder(this)
            .crossfade(true)
            .logger(
                object : Logger {
                    override var minLevel: Logger.Level = Logger.Level.Debug

                    override fun log(
                        tag: String,
                        level: Logger.Level,
                        message: String?,
                        throwable: Throwable?,
                    ) {
                        Log.i(tag, message.toString())
                    }
                },
            ).build()
}

class AppActivityLifecycleCallbacks : Application.ActivityLifecycleCallbacks by noOpDelegate() {
    override fun onActivityCreated(
        activity: Activity,
        savedInstanceState: Bundle?,
    ) {
        ActivityHierarchyManager.register(activity)
        ActivityHierarchyManager.printActivityHierarchy(BuildConfig.DEBUG)
    }

    override fun onActivityDestroyed(activity: Activity) {
        ActivityHierarchyManager.unregister(activity)
        ActivityHierarchyManager.printActivityHierarchy(BuildConfig.DEBUG)
    }
}
