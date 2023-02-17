package com.yikwing.ykextension.thread

import android.util.Log
import java.util.concurrent.*

/**
 * 搭配 CountDownLatch 使用
 * */
class DefaultPoolExecutor private constructor(
    corePoolSize: Int,
    maximumPoolSize: Int,
    keepAliveTime: Long,
    unit: TimeUnit,
    workQueue: BlockingQueue<Runnable>,
    threadFactory: ThreadFactory,
) : ThreadPoolExecutor(
    corePoolSize,
    maximumPoolSize,
    keepAliveTime,
    unit,
    workQueue,
    threadFactory,
    RejectedExecutionHandler { r, executor ->
        Log.e(
            "DefaultPoolExecutor",
            "Task rejected, too many task!",
        )
    },
) {
    /*
     *  线程执行结束，顺便看一下有么有什么乱七八糟的异常
     *
     * @param r the runnable that has completed
     * @param t the exception that caused termination, or null if
     */
    override fun afterExecute(r: Runnable?, t: Throwable?) {
        super.afterExecute(r, t)

        var throwable: Throwable? = t

        if (t == null && r is Future<*>) {
            try {
                (r as Future<*>).get()
            } catch (ce: CancellationException) {
                throwable = ce
            } catch (ee: ExecutionException) {
                throwable = ee.cause
            } catch (ie: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }

        if (throwable != null) {
            val stackTraceStr = with(StringBuilder()) {
                throwable.stackTrace.forEach { element ->
                    append("    at ").append(element.toString())
                    append("\n")
                }
                toString()
            }

            Log.e(
                "DefaultPoolExecutor",
                "Running task appeared exception! Thread [" + Thread.currentThread().name + "], because [" + throwable.message + "]\n" + stackTraceStr,
            )
        }
    }

    companion object {
        //    Thread args
        private val CPU_COUNT = Runtime.getRuntime().availableProcessors()
        private val INIT_THREAD_COUNT = CPU_COUNT + 1
        private val MAX_THREAD_COUNT = INIT_THREAD_COUNT
        private const val SURPLUS_THREAD_LIFE = 30L

        @Volatile
        private var instance: DefaultPoolExecutor? = null

        fun getInstance(): DefaultPoolExecutor {
            return instance ?: synchronized(this) {
                instance ?: DefaultPoolExecutor(
                    INIT_THREAD_COUNT,
                    MAX_THREAD_COUNT,
                    SURPLUS_THREAD_LIFE,
                    TimeUnit.SECONDS,
                    ArrayBlockingQueue(64),
                    DefaultThreadFactory(),
                ).also {
                    instance = it
                }
            }
        }
    }
}
