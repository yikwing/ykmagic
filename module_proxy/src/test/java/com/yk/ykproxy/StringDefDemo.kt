package com.yk.ykproxy

import androidx.annotation.IntDef

/**
 * @Author yikwing
 * @Date 13/2/2023-15:13
 * @Description:
 */

@IntDef(SEX.MAN, SEX.WOMAN, SEX.PRIVACY)
@Retention(AnnotationRetention.SOURCE)
annotation class SEX {
    companion object {
        const val MAN = 0x0001
        const val WOMAN = 1 shl 1
        const val PRIVACY = 1 shl 2
    }
}

fun getSexChineseStr(@SEX sex: Int): String {
    return when (sex) {
        SEX.MAN -> "男"
        SEX.WOMAN -> "女"
        SEX.PRIVACY -> "隐私"
        else -> throw Error("Not Impl")
    }
}
