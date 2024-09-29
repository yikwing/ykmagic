package com.yikwing.extension

/**
 * @Author yikwing
 * @Date 6/6/2022-14:12
 * @Description:
 */

/**
 * 获取最后几个数据
 * listOf(1,2,3).lastOf(-1)
 */
fun <T> List<T>.lastOf(num: Int): T? {
    if (num >= 0 || isEmpty()) return null
    val index = size + num
    return if (index in indices) this[index] else null
}

/**
 * 获取倒数第 num 个元素
 * arrayOf(1,2,3).lastOf(-1) // 获取倒数第1个元素
 */
fun <T> Array<out T>.lastOf(num: Int): T? {
    if (num >= 0 || isEmpty()) return null
    val index = size + num
    return if (index in indices) this[index] else null
}

/**
 * 过滤null并赋值默认值
 */
fun <T> T?.filterNullValue(defaultValue: T & Any): T & Any = this ?: defaultValue

/**
 * 过滤传入的值并赋值默认值
 */
fun <T> T?.filterValue(
    filterValue: T?,
    defaultValue: T & Any,
): T & Any = if (this == filterValue) defaultValue else this ?: defaultValue
