package com.yikwing.network

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

    fun setup(
        baseUrl: String,
        applicationInterceptor: Array<Interceptor> = emptyArray(),
        networkInterceptor: Array<Interceptor> = emptyArray(),
    ) {
        retrofit =
            Retrofit.Builder().baseUrl(baseUrl).addConverterFactory(MoshiConverterFactory.create())
                .client(initClient(applicationInterceptor, networkInterceptor)).build()
    }

    private fun initClient(
        applicationInterceptor: Array<Interceptor>,
        networkInterceptor: Array<Interceptor>,
    ): OkHttpClient {
        return OkHttpClient.Builder().apply {
            applicationInterceptor.forEach {
                addInterceptor(it)
            }
        }.addNetworkInterceptor(logger).apply {
            networkInterceptor.forEach {
                addNetworkInterceptor(it)
            }
        }.connectTimeout(10, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build()
    }

    fun <T> createService(service: Class<T>): T {
        return retrofit.create(service)
    }
}
