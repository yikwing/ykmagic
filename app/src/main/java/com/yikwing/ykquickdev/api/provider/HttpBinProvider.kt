package com.yikwing.ykquickdev.api.provider

import com.yikwing.network.RetrofitFactory
import com.yikwing.ykquickdev.api.apiserver.HttpBinApiServer
import com.yikwing.ykquickdev.api.apiserver.WanAndroidApiService

object ApiProvider {
    fun createHttpBinService(): HttpBinApiServer = RetrofitFactory.instance.createService(HttpBinApiServer::class.java)

    fun createWanAndroidService(): WanAndroidApiService = RetrofitFactory.instance.createService(WanAndroidApiService::class.java)
}
