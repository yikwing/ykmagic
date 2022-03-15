package com.yikwing.ykquickdev.api.apiserver

import com.yikwing.ykquickdev.api.entity.HttpBinHeaders
import retrofit2.http.GET

interface ApiServer {


    interface HttpBinApiServer {

        @GET("headers")
        suspend fun getHeaders(): HttpBinHeaders

    }
}