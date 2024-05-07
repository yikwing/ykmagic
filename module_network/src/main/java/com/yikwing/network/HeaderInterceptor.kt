package com.yikwing.network

import okhttp3.Interceptor
import okhttp3.Response

abstract class HeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request =
            chain.request().newBuilder()
                .apply {
                    headerList().forEach { (t, u) ->
                        addHeader(t, u)
                    }
                }.build()

        return chain.proceed(request)
    }

    abstract fun headerList(): Map<String, String>
}
