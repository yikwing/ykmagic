package com.yikwing.logger

/**
 * Used to determine how messages should be printed or saved.
 *
 * @see PrettyFormatStrategy
 */
interface FormatStrategy {
    fun log(priority: Int, tag: String?, message: String)
}