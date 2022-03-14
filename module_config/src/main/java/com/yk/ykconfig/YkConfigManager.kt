package com.yk.ykconfig

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.yk.ykconfig.annotations.YkConfigNode
import com.yk.ykconfig.annotations.YkConfigValue

/**
 * config动态配置管理类
 * */
object YkConfigManager {

    private lateinit var config: Map<String, Any>

    private val cacheConfigMap = mutableMapOf<Class<*>, Any>()

    private val moshi: Moshi = Moshi.Builder().build()
    private val mapAdapter = moshi.adapter<Map<String, Any>>(
        Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
    )


    /**
     * 初始化
     * */
    fun setUp(ykConfigStr: String) {
        config = mapAdapter.fromJson(ykConfigStr)!!
    }


    /**
     * 获取实例对象
     * */
    fun <T> getConfig(cls: Class<T>): T {
        return if (cacheConfigMap.containsKey(cls)) {
            cacheConfigMap[cls] as T
        } else {
            val ykConfigNode = cls.getAnnotation(YkConfigNode::class.java)
            assert(ykConfigNode != null)

            val t = getConfigNode(cls)
            cacheConfigMap[cls] = t!!
            t
        }
    }


    /**
     * 遍历拿出config值
     * */
    private fun <T> getJsonElement(path: String): T? {

        val splits = path.split(".")

        var baseConfigModel = config

        var result: T? = null

        splits.forEachIndexed { index, s ->
            if (index == splits.size - 1) {
                result = baseConfigModel[s] as T
            } else {
                baseConfigModel = baseConfigModel[s] as Map<String, Any>
            }
        }

        return result
    }


    /**
     * 配置new json对象
     * */
    private fun <T> getConfigNode(cls: Class<T>): T {
        val node = mutableMapOf<String, Any?>()

        val fields = cls.declaredFields

        fields.forEach { field ->
            val ykConfigValue = field.getAnnotation(YkConfigValue::class.java)
            ykConfigValue?.let {
                val configName = it.name

                when (field.type) {
                    String::class.java -> {
                        val value = getJsonElement<String>(configName)
                        node[field.name] = value
                    }
                    Boolean::class.java -> {
                        val value = getJsonElement<Boolean>(configName)
                        node[field.name] = value
                    }
                    Int::class.java -> {
                        val value = getJsonElement<Double>(configName)
                        node[field.name] = value?.toInt()
                    }
                    Double::class.java -> {
                        val value = getJsonElement<Double>(configName)
                        node[field.name] = value
                    }
                    else -> {
                        throw  Exception("Not Found")
                    }
                }

            }

        }

        return moshi.adapter(cls).fromJson(mapAdapter.toJson(node as Map<String, Any>))!!
    }

}