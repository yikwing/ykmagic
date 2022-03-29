package com.yikwing.logger

import android.util.Log
import com.yikwing.logger.LogStrategy
import com.yikwing.logger.LogcatLogStrategy

/**
 * LogCat implementation for [LogStrategy]
 *
 *
 * This simply prints out all logs to Logcat by using standard [Log] class.
 */
class LogcatLogStrategy : LogStrategy {
    override fun log(priority: Int, tag: String?, message: String) {
        Log.println(priority, tag ?: DEFAULT_TAG, message)
    }

    companion object {
        const val DEFAULT_TAG = "NO_TAG"
    }
}