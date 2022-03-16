package com.yikwing.ykquickdev.api.provider

import com.yikwing.ykquickdev.api.apiserver.HttpBinApiServer
import com.yikwing.ykquickdev.api.apiserver.WanAndroidApiService
import com.yk.yknetwork.RetrofitFactory

object HttpBinProvider {
    fun providerHeader(): HttpBinApiServer =
        RetrofitFactory.instance.createService(HttpBinApiServer::class.java)
}


object WanAndroidProvider {
    fun providerWanAndroid(): WanAndroidApiService =
        RetrofitFactory.instance.createService(WanAndroidApiService::class.java)
}