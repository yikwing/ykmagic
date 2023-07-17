package com.yikwing.proxy

const val snow = 0x0001
const val rain = 0x0002
const val sun = 0x0004
const val cloud = 0x0008
const val STATUS_5 = 0x0010
const val STATUS_6 = 0x0020
const val STATUS_7 = 0x0040
const val STATUS_8 = 0x0080
const val STATUS_9 = 0x0100
const val STATUS_10 = 0x0200
const val STATUS_11 = 0x0400
const val STATUS_12 = 0x0800

// snow rain
const val WEATHER_A = snow or rain

// snow
const val WEATHER_B = WEATHER_A and rain.inv() or sun

const val W_A_IS_RAIN = (WEATHER_A and rain) != 0
const val W_B_IS_RAIN = (WEATHER_B and rain) != 0
const val W_B_IS_SUN = (WEATHER_B and sun) != 0

/**
 * 当我们需要往状态集中添加状态时，就通过或运算。例如：
 * STATUSES | STATUS_1
 * */

/**
 * 当我们需要从状态集中移除状态时，就通过取反运算。例如：
 * STATUSES & ~ STATUS_1
 * */

/**
 * 当我们需要判断状态集中是否包含某状态时，就通过与运算。结果为 0 即代表无，反之有。
 * (statuses & status) != 0
 * */
