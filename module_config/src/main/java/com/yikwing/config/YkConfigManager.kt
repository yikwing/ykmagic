package com.yikwing.config

import kotlinx.serialization.json.Json

/**
 * 配置管理器
 *
 * 使用 kotlinx.serialization 静态解析 JSON 配置
 */
object YkConfigManager {
    private var _config: AppConfig? = null

    val config: AppConfig
        get() = checkNotNull(_config) { "YkConfigManager 未初始化，请先调用 setUp()" }

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * 初始化配置
     * @param configJson JSON 配置字符串
     */
    fun setUp(configJson: String) {
        _config = json.decodeFromString<AppConfig>(configJson)
    }

    /**
     * 检查是否已初始化
     */
    val isInitialized: Boolean
        get() = _config != null
}
