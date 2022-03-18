package com.yk.ykconfig

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson


/**
 *
 * Moshi.Builder().add(SexAdapter()).build()
 *
 * */
class SexAdapter {
    @FromJson
    fun fromJson(value: Int): Sex {
        return when (value) {
            -1 -> Sex.FEMALE
            0 -> Sex.NORMAL
            1 -> Sex.MALE
            else -> throw RuntimeException("Not Found")
        }
    }

    @ToJson
    fun toJson(sex: Sex): Int {
        return when (sex) {
            Sex.FEMALE -> -1
            Sex.NORMAL -> 0
            Sex.MALE -> 1
        }
    }

}