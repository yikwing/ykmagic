package com.yikwing.ykquickdev.api.apiserver

import com.yikwing.ykquickdev.api.entity.ChaptersBean
import com.yikwing.ykquickdev.api.entity.HttpBinHeaders
import com.yk.yknetwork.BaseHttpResult
import retrofit2.http.GET

interface HttpBinApiServer {
    @GET("headers")
    suspend fun getHeaders(): HttpBinHeaders
}


interface WanAndroidApiService {
    @GET("wxarticle/chapters/json")
    suspend fun getChapters(): BaseHttpResult<List<ChaptersBean>>
}