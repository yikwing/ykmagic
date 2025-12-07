package com.yikwing.ykquickdev.api.apiserver

import com.yikwing.network.BaseHttpResult
import com.yikwing.ykquickdev.api.entity.ChapterBean
import com.yikwing.ykquickdev.api.entity.HttpBinHeaders
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.appendPathSegments
import io.ktor.http.takeFrom
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

class Repo
    @Inject
    constructor(
        private val httpClient: HttpClient,
    ) {
        suspend fun binGet(): HttpBinHeaders =
            httpClient
                .get {
                    url {
                        takeFrom("https://httpbin.org")
                        appendPathSegments("get")
                        parameters.append("token", "abc123")
                    }
                }.body()

        suspend fun binPost(): JsonElement =
            httpClient
                .post {
                    url {
                        takeFrom("https://httpbin.org")
                        appendPathSegments("post")
                        parameters.append("token", "abc123")
                    }
                    setBody(
                        buildJsonObject {
                            put("zs", 23)
                        },
                    )
                }.body()

        suspend fun getChapters(): BaseHttpResult<List<ChapterBean>> =
            httpClient
                .get {
                    url("wxarticle/chapters/json")
                }.body()
    }
