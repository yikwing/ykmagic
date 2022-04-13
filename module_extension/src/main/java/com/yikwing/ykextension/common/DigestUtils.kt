package com.yikwing.ykextension.common

import java.security.MessageDigest

/**
 * @Author yikwing
 * @Date 13/4/2022-13:30
 * @Description: MD1 SHA1 算法
 */
object DigestUtils {
    const val MD5 = "MD5"
    const val SHA1 = "SHA-1"

    /**
     * @param str 待计算的文本
     * @param algorithm 算法
     */
    fun crypto(str: String, algorithm: String): String {
        return crypto(str.toByteArray(), algorithm)
    }

    /**
     * 小写
     */
    fun crypto(buffer: ByteArray, algorithm: String): String {
        val cryptoResult = MessageDigest.getInstance(algorithm).run {
            update(buffer)
            digest()
        }

        return cryptoResult.joinToString(separator = "") { byte -> "%02x".format(byte) }
    }

    /**
     * 大写并以:分隔
     */
    fun crypto2capital(buffer: ByteArray, algorithm: String): String {
        val cryptoResult = MessageDigest.getInstance(algorithm).run {
            update(buffer)
            digest()
        }

        return cryptoResult.joinToString(separator = ":") { byte -> "%02x".format(byte) }.toUpperCase()
    }
}