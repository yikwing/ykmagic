package com.yikwing.api.apiserver

import com.yikwing.api.entity.HttpBinHeaders
import kotlinx.coroutines.flow.Flow
import retrofit2.http.GET

interface ApiServer {


    interface HttpBinApiServer {

        @GET("headers")
        suspend fun getHeaders(): HttpBinHeaders

    }
}