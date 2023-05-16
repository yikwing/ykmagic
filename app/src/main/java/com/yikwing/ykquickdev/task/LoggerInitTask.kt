package com.yikwing.ykquickdev.task

import android.app.Application
import android.content.Context
import com.yk.ykproxy.startup.Initializer
import com.yikwing.logger.AndroidLogAdapter
import com.yikwing.logger.Logger
import com.yikwing.logger.PrettyFormatStrategy
import com.yikwing.ykquickdev.BuildConfig

class LoggerInitTask : Initializer<Unit> {
    override fun create(context: Application) {
        Logger.addLogAdapter(
            object : AndroidLogAdapter(
                PrettyFormatStrategy.newBuilder().tag("yk").build(),
            ) {
                override fun isLoggable(priority: Int, tag: String?): Boolean {
                    return BuildConfig.DEBUG
                }
            },
        )
    }

    override fun dependencies(): Set<Class<out Initializer<*>>> = emptySet()
}
