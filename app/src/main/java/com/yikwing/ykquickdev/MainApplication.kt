package com.yikwing.ykquickdev

import android.app.Application
import com.yk.ykconfig.SmallConfig
import com.yk.ykconfig.YkConfigManager
import com.yk.ykconfig.YkQuickManager
import com.yk.yknetwork.RetrofitFactory

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()


        initSetup()


    }

    private fun initSetup() {
        YkQuickManager.setUp(
            this, """
            {
                "base_url":"https://httpbin.org"
            }
        """.trimIndent()
        )
        RetrofitFactory.instance.setup(
            YkConfigManager.getConfig(SmallConfig::class.java).baseUrl
        )
    }

}