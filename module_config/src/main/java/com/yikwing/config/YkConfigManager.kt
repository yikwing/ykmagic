package com.yikwing.config

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.yikwing.config.annotations.YkConfigNode
import com.yikwing.config.annotations.YkConfigValue

/**
 * config动态配置管理类
 * */
object YkConfigManager {
    private lateinit var config: Map<String, Any>

    private val cacheConfigMap = mutableMapOf<Class<*>, Any>()

    private val moshi: Moshi = Moshi.Builder().build()

    private val mapAdapter =
        moshi.adapter<Map<String, Any>>(
            Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java),
        )

    /**
     * 初始化
     * */
    fun setUp(ykConfigStr: String) {
        config = mapAdapter.fromJson(ykConfigStr) ?: emptyMap()
    }

    /**
     * 获取实例对象
     * */
    fun <T> getConfig(cls: Class<T>): T & Any =
        cacheConfigMap
            .getOrPut(cls) {
                val ykConfigNode = cls.getAnnotation(YkConfigNode::class.java)
                requireNotNull(ykConfigNode) { "Class ${cls.name} is missing @YkConfigNode annotation" }

                val finalNode = mutableMapOf<String, Any?>()

                cls.declaredFields.forEach { field ->
                    var tmpNode = config
                    val ykConfigValue = field.getAnnotation(YkConfigValue::class.java)
                    ykConfigValue?.let {
                        val nodePath = it.path
                        val splits = nodePath.split(".")
                        splits.forEachIndexed { index, s ->
                            if (index == splits.size - 1) {
                                val result =
                                    when (field.type) {
                                        String::class.java -> tmpNode.getOrDefault(s, "")
                                        Int::class.java -> tmpNode.getOrDefault(s, -1)
                                        Double::class.java -> tmpNode.getOrDefault(s, -1.0)
                                        Boolean::class.java -> tmpNode.getOrDefault(s, false)
                                        else -> throw Error("Not impl Type")
                                    }
                                finalNode.put(field.name, result)
                            } else {
                                tmpNode = tmpNode.get(s) as Map<String, Any>
                            }
                        }
                    }
                }

                moshi.adapter(cls).fromJsonValue(finalNode)
                    ?: throw IllegalArgumentException("Failed to parse config for class '${cls.name}'")
            }.let { t ->
                cls.cast(t)
            }!!
}
