package com.yk.yknetwork

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitFactory private constructor() {

    private lateinit var retrofit: Retrofit

    companion object {
        val instance by lazy(LazyThreadSafetyMode.NONE) {
            RetrofitFactory()
        }
    }

    private val logger by lazy(LazyThreadSafetyMode.NONE) {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    fun setup(baseUrl: String, interceptor: Array<Interceptor>) {
        retrofit =
            Retrofit.Builder().baseUrl(baseUrl).addConverterFactory(MoshiConverterFactory.create())
                .client(initClient(interceptor)).build()
    }

    private fun initClient(interceptorList: Array<Interceptor>): OkHttpClient {
        return OkHttpClient.Builder().addNetworkInterceptor(logger).apply {
            interceptorList.forEach {
                addInterceptor(it)
            }
        }.connectTimeout(10, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build()
    }

    fun <T> createService(service: Class<T>): T {
        return retrofit.create(service)
    }
}
