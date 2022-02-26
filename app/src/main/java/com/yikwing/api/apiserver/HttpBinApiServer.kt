package com.yikwing.api.apiserver

import com.yikwing.api.entity.HttpBinHeaders
import retrofit2.http.GET

interface ApiServer {


    interface HttpBinApiServer {

        @GET("headers")
        suspend fun getHeaders(): HttpBinHeaders

    }
}