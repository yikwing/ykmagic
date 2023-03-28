package com.yikwing.ykquickdev.task

import android.content.Context
import com.example.studydemo.startup.Initializer
import com.yikwing.logger.AndroidLogAdapter
import com.yikwing.logger.Logger
import com.yikwing.logger.PrettyFormatStrategy
import com.yikwing.ykquickdev.BuildConfig

class LoggerInitTask : Initializer<Unit> {
    override fun create(context: Context) {
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
