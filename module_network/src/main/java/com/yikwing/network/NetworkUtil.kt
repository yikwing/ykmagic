package com.yikwing.network

import java.net.ProxySelector
import java.net.URI

/**
 * 检查代理
 * return true: 使用了代理
 * return false: 没有使用代理
 * */
fun checkProxy(): Boolean {
    // 检查 HTTP 代理
    val httpProxyHost = System.getProperty("http.proxyHost")
    val httpProxyPort = System.getProperty("http.proxyPort")

    if (httpProxyHost != null && httpProxyPort != null) {
        return true
    }

    // 检查 HTTPS 代理
    val httpsProxyHost = System.getProperty("https.proxyHost")
    val httpsProxyPort = System.getProperty("https.proxyPort")

    if (httpsProxyHost != null && httpsProxyPort != null) {
        return true
    }

    // 检查 SOCKS 代理
    val socksProxyHost = System.getProperty("socksProxyHost")
    val socksProxyPort = System.getProperty("socksProxyPort")

    if (socksProxyHost != null && socksProxyPort != null) {
        return true
    }

    // 检查 ProxySelector 的默认代理设置
    val defaultProxySelector = ProxySelector.getDefault()
    val proxies = defaultProxySelector.select(URI("http://www.example.com"))
    if (proxies.isNotEmpty() && proxies[0].type() != java.net.Proxy.Type.DIRECT) {
        return true
    }

    // 检查环境变量
    val httpProxyEnv = System.getenv("HTTP_PROXY") ?: System.getenv("http_proxy")
    val httpsProxyEnv = System.getenv("HTTPS_PROXY") ?: System.getenv("https_proxy")

    return !httpProxyEnv.isNullOrEmpty() || !httpsProxyEnv.isNullOrEmpty()
}
