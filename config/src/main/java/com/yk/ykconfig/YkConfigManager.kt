package com.yk.ykconfig

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.yk.ykconfig.annotations.YkConfigNode
import com.yk.ykconfig.annotations.YkConfigValue

object YkConfigManager {

    private lateinit var config: JsonElement

    private val cacheConfigMap = mutableMapOf<Class<*>, Any>()

    fun setUp(ykConfigStr: String) {
        config = JsonParser.parseString(ykConfigStr)
    }

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

    private fun getJsonElement(path: String): JsonElement {

        val splits = path.split(".")

        var result = config


        splits.forEach {
            result = result.asJsonObject.get(it)
        }


        return result


    }

    private fun <T> getConfigNode(cls: Class<T>): T {
        val node = JsonObject()

        val fields = cls.declaredFields

        fields.forEach { field ->
            val ykConfigValue = field.getAnnotation(YkConfigValue::class.java)
            ykConfigValue?.let {
                val configName = it.name
                val value = getJsonElement(configName)

                when (field.type) {
                    String::class.java -> {
                        node.addProperty(field.name, value.asString)
                    }
                    Boolean::class.java -> {
                        node.addProperty(field.name, value.asBoolean)
                    }
                    Int::class.java -> {
                        node.addProperty(field.name, value.asInt)
                    }
                    Long::class.java -> {
                        node.addProperty(field.name, value.asLong)
                    }
                    Float::class.java -> {
                        node.addProperty(field.name, value.asFloat)
                    }
                    Double::class.java -> {
                        node.addProperty(field.name, value.asDouble)
                    }
                    else -> {
                        node.addProperty(field.name, value.asNumber)
                    }
                }

            }

        }

        return Gson().fromJson(node, cls)

    }

}