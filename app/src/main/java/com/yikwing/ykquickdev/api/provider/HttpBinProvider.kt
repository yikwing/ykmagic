package com.yikwing.ykquickdev.api.provider

import com.yikwing.ykquickdev.api.apiserver.ApiServer
import com.yk.yknetwork.RetrofitFactory

object HttpBinProvider {
    fun providerHeader(): ApiServer.HttpBinApiServer =
        RetrofitFactory.instance.createService(ApiServer.HttpBinApiServer::class.java)
}