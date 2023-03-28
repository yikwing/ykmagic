package com.yikwing.ykquickdev

import android.app.Application
import com.yk.ykproxy.startup.AppInitializer
import com.yikwing.ykquickdev.task.ConfigInjectInitTask
import com.yikwing.ykquickdev.task.LoggerInitTask
import com.yikwing.ykquickdev.task.NetworkInitTask

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        initSetup()
    }

    private fun initSetup() {
        AppInitializer.getInstance(this).addTask(ConfigInjectInitTask()).addTask(LoggerInitTask())
            .addTask(NetworkInitTask()).build(debug = true)
    }
}
