package com.yk.yknetwork

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitFactory private constructor() {

    private lateinit var retrofit: Retrofit

    companion object {
        val instance by lazy {
            RetrofitFactory()
        }
    }

    private val logger by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    fun setup(baseUrl: String, context: Context) {
        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create())
            .client(initClient(context))
            .build()
    }

    private fun initClient(context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .addNetworkInterceptor(logger)
            .addInterceptor(ChuckerInterceptor(context))
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
    }


    fun <T> createService(service: Class<T>): T {
        return retrofit.create(service)
    }

}