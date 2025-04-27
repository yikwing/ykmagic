package com.yikwing.extension.plus

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy

/**
 * <pre>
 *     author: yikwing
 *
 *        _ _              _
 *       (_) |            (_)
 *  _   _ _| | ____      ___ _ __   __ _
 * | | | | | |/ /\ \ /\ / / | '_ \ / _` |
 * | |_| | |   <  \ V  V /| | | | | (_| |
 *  \__, |_|_|\_\  \_/\_/ |_|_| |_|\__, |
 *   __/ |                          __/ |
 *  |___/                          |___/
 *
 *     email : 49999@live.com
 *     time  : 2025-04-27 23:09
 *     desc  : Proxy Plus
 * </pre>
 */
inline fun <reified T : Any> noOpDelegate(): T {
    val javaClass = T::class.java
    return Proxy.newProxyInstance(
        javaClass.classLoader,
        arrayOf(javaClass),
        NO_OP_HANDLER,
    ) as T
}

val NO_OP_HANDLER =
    InvocationHandler { _, _, _ ->
        // no op
    }
