package com.yikwing.ykquickdev

import android.app.Application
import com.yikwing.ykquickdev.task.ConfigInjectInitTask
import com.yikwing.ykquickdev.task.DataStoreInitTask
import com.yikwing.ykquickdev.task.LoggerInitTask
import com.yikwing.ykquickdev.task.NetworkInitTask
import com.yk.ykproxy.startup.AppInitializer

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        initSetup()
    }

    private fun initSetup() {
        AppInitializer.getInstance(this)
            .addTask(ConfigInjectInitTask())
            .addTask(LoggerInitTask())
            .addTask(NetworkInitTask())
            .addTask(DataStoreInitTask())
            .build(debug = true)
    }
}
