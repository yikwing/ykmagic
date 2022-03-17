package com.yikwing.ykquickdev

import android.app.Application
import com.yk.ykconfig.YkConfigManager
import com.yk.ykconfig.YkQuickManager
import com.yk.yknetwork.RetrofitFactory

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        initSetup()
    }

    private fun initSetup() {

        //  初始化配置
        YkQuickManager.setUp(
            this, BuildConfig.YK_CONFIG
        )

        //  初始化网络请求base url
        RetrofitFactory.instance.setup(
            YkConfigManager.getConfig(NetworkConfig::class.java).baseUrl
        )

    }

}