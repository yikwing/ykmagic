package com.yikwing.ykquickdev.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun moshi(): Moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    @Singleton
    @Provides
    fun json(): Json =
        Json {
            // 宽松模式，允许解析格式不规范的 JSON（如尾随逗号、未转义的引号）
            isLenient = true
            // 忽略 JSON 中未在数据类中定义的字段，避免因 API 新增字段导致的反序列化失败
            ignoreUnknownKeys = true
            // 强制将不兼容的类型转换为期望值（如将 JSON 数字字符串自动转为数字类型）
            coerceInputValues = true
            // 不序列化 null 值，减少 JSON 体积，兼容后端 nullable 字段
            explicitNulls = false
        }
}
