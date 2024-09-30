package com.yikwing.network

import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
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
        val json =
            Json {
                ignoreUnknownKeys = true // 忽略未知字段
//                isLenient = true // 支持宽松的解析，例如多余的逗号
                prettyPrint = true // 美化输出（可选）
            }

        // 定义媒体类型
        val contentType = "application/json; charset=UTF-8".toMediaType()

        retrofit =
            Retrofit
                .Builder()
                .baseUrl(baseUrl)
//                .addConverterFactory(MoshiConverterFactory.create())
                .addConverterFactory(
                    json.asConverterFactory(contentType),
                ).client(initClient(applicationInterceptor, networkInterceptor))
                .build()
    }

    private fun initClient(
        applicationInterceptor: Array<Interceptor>,
        networkInterceptor: Array<Interceptor>,
    ): OkHttpClient =
        OkHttpClient
            .Builder()
            .apply {
                // 添加应用层拦截器
                applicationInterceptor.forEach { addInterceptor(it) }

                // 添加网络层拦截器
                networkInterceptor.forEach { addNetworkInterceptor(it) }

                // 添加日志拦截器
                addInterceptor(logger)

                // 设置超时
                connectTimeout(10, TimeUnit.SECONDS)
                readTimeout(10, TimeUnit.SECONDS)
            }.build()

    fun <T> createService(service: Class<T>): T = retrofit.create(service)
}
