package com.yikwing.ykquickdev.utils

import java.security.MessageDigest


object DigestUtils {
    const val MD5 = "MD5"
    const val SHA1 = "SHA-1"

    fun crypto(str: String, algorithm: String): String {
        return crypto(str.toByteArray(), algorithm)
    }

    fun crypto(buffer: ByteArray, algorithm: String): String {
        val cryptoResult = MessageDigest.getInstance(algorithm).run {
            update(buffer)
            digest()
        }

        return cryptoResult.joinToString(separator = "") { byte -> "%02x".format(byte) }
    }

    fun crypto2capital(buffer: ByteArray, algorithm: String): String {
        val cryptoResult = MessageDigest.getInstance(algorithm).run {
            update(buffer)
            digest()
        }

        return cryptoResult.joinToString(separator = ":") { byte -> "%02x".format(byte) }.toUpperCase()
    }
}