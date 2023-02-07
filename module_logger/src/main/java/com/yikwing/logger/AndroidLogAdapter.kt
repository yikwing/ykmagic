package com.yikwing.logger

/**
 * Android terminal log output implementation for [LogAdapter].
 *
 *
 * Prints output to LogCat with pretty borders.
 *
 * <pre>
 * ┌──────────────────────────
 * │ Method stack history
 * ├┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄
 * │ Log message
 * └──────────────────────────
</pre> *
 */
open class AndroidLogAdapter(
    private val formatStrategy: FormatStrategy =
        PrettyFormatStrategy.newBuilder().build(),
) : LogAdapter {

    override fun isLoggable(priority: Int, tag: String?): Boolean {
        return true
    }

    override fun log(priority: Int, tag: String?, message: String) {
        formatStrategy.log(priority, tag, message)
    }
}
