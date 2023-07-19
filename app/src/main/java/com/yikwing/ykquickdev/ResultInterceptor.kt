package com.yikwing.ykquickdev

import com.yikwing.network.HeaderInterceptor

class ResultInterceptor : HeaderInterceptor() {
    override fun headerList(): Map<String, String> {
        return mapOf(
            Pair("version", BuildConfig.VERSION_NAME),
            Pair("User-Agent", "${BuildConfig.APPLICATION_ID}_${BuildConfig.VERSION_NAME}")
        )
    }
}
