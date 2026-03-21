package com.yikwing.ykquickdev.api.apiserver

import com.yikwing.ykquickdev.api.entity.HttpBinHeaders
import com.yikwing.ykquickdev.api.entity.HttpBinPostResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.appendPathSegments
import io.ktor.http.takeFrom
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.koin.core.annotation.Singleton

@Singleton
class HttpBinApi(
    private val httpClient: HttpClient,
) {
    suspend fun getHeaders(token: String = DEFAULT_TOKEN): HttpBinHeaders =
        httpClient
            .get {
                url {
                    takeFrom(BASE_URL)
                    appendPathSegments("get")
                    parameters.append("token", token)
                }
            }.body()

    suspend fun postData(token: String = DEFAULT_TOKEN): HttpBinPostResult =
        httpClient
            .post {
                url {
                    takeFrom(BASE_URL)
                    appendPathSegments("post")
                    parameters.append("token", token)
                }
                setBody(
                    buildJsonObject {
                        put("zs", 23)
                    },
                )
            }.body()

    private companion object {
        const val BASE_URL = "https://httpbin.org"
        const val DEFAULT_TOKEN = "abc123"
    }
}
