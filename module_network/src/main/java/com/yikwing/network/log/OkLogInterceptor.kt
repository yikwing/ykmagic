package com.yikwing.network.log

import jakarta.inject.Inject
import jakarta.inject.Singleton
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * OkHttp 日志拦截器
 *
 * 追踪请求/响应并格式化输出日志
 */
@Singleton
class OkLogInterceptor
    @Inject
    constructor() : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val requestId = RequestTracker.startRequest(request)

            return try {
                val response = chain.proceed(request)
                RequestTracker.completeRequest(requestId, response)
                response
            } catch (e: Exception) {
                RequestTracker.failRequest(requestId, e)
                throw e
            }
        }
    }
