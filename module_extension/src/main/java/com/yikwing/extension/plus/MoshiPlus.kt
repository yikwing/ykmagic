package com.yikwing.extension.plus

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

val moshi: Moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

// 扩展函数，用于获取 List<T> 的 JsonAdapter
inline fun <reified T> Moshi.listAdapter(): JsonAdapter<List<T>> {
    val parameterizedType = Types.newParameterizedType(List::class.java, T::class.java)
    return adapter(parameterizedType)
}

// 扩展函数，用于获取 Map<K, V> 的 JsonAdapter
inline fun <reified K, reified V> Moshi.mapAdapter(): JsonAdapter<Map<K, V>> {
    val parameterizedType =
        Types.newParameterizedType(Map::class.java, K::class.java, V::class.java)
    return adapter(parameterizedType)
}

// 美化json输出
inline fun <reified T> prettierJson(obj: T): String {
    val jsonAdapter = moshi.adapter(T::class.java).indent("  ")
    return jsonAdapter.toJson(obj)
}
