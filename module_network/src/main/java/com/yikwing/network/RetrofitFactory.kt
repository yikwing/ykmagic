package com.yikwing.network

import android.content.Context
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
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

    fun setup(
        context: Context,
        baseUrl: String,
        applicationInterceptor: List<Interceptor> = emptyList(),
        networkInterceptor: List<Interceptor> = emptyList(),
    ) {
        retrofit =
            Retrofit
                .Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(MoshiConverterFactory.create())
                .client(initClient(context, applicationInterceptor, networkInterceptor))
                .build()
    }

    private fun initClient(
        context: Context,
        applicationInterceptor: List<Interceptor>,
        networkInterceptor: List<Interceptor>,
    ): OkHttpClient =
        OkHttpClient
            .Builder()
            .apply {
                // 添加日志拦截器
                // 如果放在重试拦截器后面，每次重试都会打印日志
                addInterceptor(OkLogInterceptor())

                // 添加应用层拦截器
                applicationInterceptor.forEach { addInterceptor(it) }

                // 添加重试拦截器
                addInterceptor(RetryInterceptor())

                // 添加网络层拦截器
                networkInterceptor.forEach { addNetworkInterceptor(it) }

                callTimeout(30, TimeUnit.SECONDS)
                connectTimeout(15, TimeUnit.SECONDS)
                readTimeout(15, TimeUnit.SECONDS)
                writeTimeout(15, TimeUnit.SECONDS)

                cache(Cache(context.cacheDir, maxSize = 10 * 1024 * 1024))
            }.build()

    fun <T> createService(service: Class<T>): T = retrofit.create(service)
}
