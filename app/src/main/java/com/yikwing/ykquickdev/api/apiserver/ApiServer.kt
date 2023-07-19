package com.yikwing.ykquickdev.api.apiserver

import com.yikwing.network.BaseHttpResult
import com.yikwing.ykquickdev.api.entity.ChapterBean
import com.yikwing.ykquickdev.api.entity.HttpBinHeaders
import retrofit2.http.GET
import retrofit2.http.Url

interface HttpBinApiServer {
    @GET("headers")
    suspend fun getHeaders(): HttpBinHeaders

    @GET
    suspend fun getOtherHeaders(
        @Url url: String = "https://httpbin.org/headers"
    ): HttpBinHeaders
}

interface WanAndroidApiService {
    @GET("wxarticle/chapters/json")
    suspend fun getChapters(): BaseHttpResult<List<ChapterBean>>
}
