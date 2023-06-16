package com.yk.ykconfig

import org.junit.Test

/**
 * return 退出函数
 * return@forEach continue
 * return@loop break
 *
 * */
class ReturnsUnitTest {

    @Test
    fun return_func() {
        val list = listOf(1, 2, 3, 4, 5, 6, 7, 8)

        run loop@{
            list.forEach {
                if (it == 3) {
                    return@loop
                }

                println(it)
            }
        }

        println("exit")
    }
}
