package com.yikwing.api.provider

import com.yikwing.api.apiserver.ApiServer
import com.yk.yknetwork.RetrofitFactory

object HttpBinProvider {
    fun providerHeader(): ApiServer.HttpBinApiServer =
        RetrofitFactory.instance.createService(ApiServer.HttpBinApiServer::class.java)
}