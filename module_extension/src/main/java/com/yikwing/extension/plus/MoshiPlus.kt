package com.yikwing.extension.plus

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

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
fun prettierJson(json: String): String {
    val moshi = Moshi.Builder().build()
    val mapAdapter = moshi.mapAdapter<String, Any>().indent("  ")
    val map = mapAdapter.fromJson(json)
    return mapAdapter.toJson(map)
}
