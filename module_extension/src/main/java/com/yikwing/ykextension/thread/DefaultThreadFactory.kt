package com.yikwing.ykextension.thread

import android.util.Log
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

/**
 * 线程池工厂类
 */
class DefaultThreadFactory : ThreadFactory {
    private val threadNumber = AtomicInteger(1)
    private val group: ThreadGroup
    private val namePrefix: String

    init {
        val s = System.getSecurityManager()
        group = if (s != null) s.threadGroup else Thread.currentThread().threadGroup!!
        namePrefix = "ARouter task pool No." + poolNumber.getAndIncrement() + ", thread No."
    }

    override fun newThread(runnable: Runnable): Thread {
        val threadName = namePrefix + threadNumber.getAndIncrement()
        Log.e("DefaultThreadFactory", "Thread production, name is [$threadName]")
        val thread = Thread(group, runnable, threadName, 0)
        if (thread.isDaemon) {
            // 设为非后台线程
            thread.isDaemon = false
        }
        if (thread.priority != Thread.NORM_PRIORITY) {
            // 优先级为normal
            thread.priority = Thread.NORM_PRIORITY
        }

        // 捕获多线程处理中的异常
        thread.uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { t, e ->
            Log.e(
                "DefaultThreadFactory",
                "Running task appeared exception! Thread [" + t.name + "], because [" + e.message + "]",
            )
        }

        return thread
    }

    companion object {
        private val poolNumber = AtomicInteger(1)
    }
}
