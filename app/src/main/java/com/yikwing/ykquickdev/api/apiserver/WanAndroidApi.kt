package com.yikwing.ykquickdev.api.apiserver

import com.yikwing.network.BaseHttpResult
import com.yikwing.ykquickdev.api.entity.ChapterBean
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.url
import org.koin.core.annotation.Singleton

@Singleton
class WanAndroidApi(
    // Lazy defers CIO engine + TLS setup from DI resolution (main thread, on screen open) to the first request.
    private val httpClient: Lazy<HttpClient>,
) {
    suspend fun getChapters(): BaseHttpResult<List<ChapterBean>> =
        httpClient.value
            .get {
                url("wxarticle/chapters/json")
            }.body()
}
