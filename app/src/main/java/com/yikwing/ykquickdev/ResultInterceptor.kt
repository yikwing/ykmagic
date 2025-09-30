package com.yikwing.ykquickdev

import com.yikwing.network.BaseHeaderInterceptor

class HeaderInterceptor : BaseHeaderInterceptor() {
    override fun headerList(): Map<String, String> =
        mapOf(
            Pair("version", BuildConfig.VERSION_NAME),
            Pair("User-Agent", "${BuildConfig.APPLICATION_ID}_${BuildConfig.VERSION_NAME}"),
        )
}
