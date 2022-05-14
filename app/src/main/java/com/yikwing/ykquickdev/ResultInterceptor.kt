package com.yikwing.ykquickdev

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody


class ResultInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {

        // add cached cookie into request
        val originRequest = chain.request()
        val newBuilder = originRequest.newBuilder()
        newBuilder.addHeader("a", "b")
        val request = newBuilder.build()

        // get response
        val response = chain.proceed(request)

        val body = response.body
        val responseBodyString = body.string()

        val contentType = response.body.contentType()
        val responseBody = """
            {
                "data": [
                    {
                        "lisenseLink": "",
                        "parentChapterId": 407,
                        "name": "\u9e3f\u6d0b",
                        "lisense": "",
                        "desc": "",
                        "cover": "",
                        "id": 408,
                        "children": [],
                        "author": "",
                        "order": 190000,
                        "userControlSetTop": false,
                        "courseId": 13,
                        "visible": 1
                    }
                ],
                "errorCode": 0,
                "errorMsg": ""
            }
        """.trimIndent().toResponseBody(contentType)
        val originResponse = response.newBuilder().body(responseBody).build()

        return originResponse

    }
}