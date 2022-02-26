package com.yk.ykconfig

import android.app.Application

object YkQuickManager {

    lateinit var app: Application
    private var isSetUp: Boolean = false

    fun setUp(application: Application, ykConfigStr: String) {
        YkQuickManager.app = application
        isSetUp = true
        YkConfigManager.setUp(ykConfigStr)
    }

    fun getApplication(): Application {
        if (!isSetUp) {
            throw  IllegalArgumentException("应用 没有调用 setup 初始化")
        } else {
            return app
        }
    }

}