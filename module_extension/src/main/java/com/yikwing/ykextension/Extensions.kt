package com.yikwing.ykextension

/**
 * @Author yikwing
 * @Date 6/6/2022-14:12
 * @Description:
 */

import kotlin.math.abs

/**
 * 获取最后几个数据
 * listOf(1,2,3).lastNum(-1)
 */
fun <T> List<T>.lastNum(num: Int): T? {
    val absNum = abs(num)
    return if (isEmpty() || num >= 0 || size < absNum) null else this[size - absNum]
}

/**
 * 获取最后几个数据
 * arrayOf(1,2,3).lastNum(-1)
 */
fun <T> Array<out T>.lastNum(num: Int): T? {
    val absNum = abs(num)
    return if (isEmpty() || num >= 0 || size < absNum) null else this[size - absNum]
}

/**
 * 过滤null并赋值默认值
 */
fun <T> T.filterNullValue(defaultValue: T & Any): T & Any {
    return filterValue(null, defaultValue)
}

/**
 * 过滤传入的值并赋值默认值
 */
fun <T> T.filterValue(filterValue: T, defaultValue: T & Any): T & Any {
    return if (this === filterValue) defaultValue else this ?: defaultValue
}
