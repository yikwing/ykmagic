package com.yikwing.extension.util

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * DigestUtils 单元测试
 *
 * 用 NIST FIPS 180 公开测试向量作为 fixture，锁定输出格式（小写十六进制 /
 * 冒号分隔大写），防止对 `%02x` 格式化逻辑的意外修改引发回归。
 */
class DigestUtilsTest {
    // 参考向量来自 FIPS 180 及 RFC 1321：空串与 "abc" 是规范里最常引用的已知答案
    private val emptyMd5 = "d41d8cd98f00b204e9800998ecf8427e"
    private val abcMd5 = "900150983cd24fb0d6963f7d28e17f72"
    private val emptySha1 = "da39a3ee5e6b4b0d3255bfef95601890afd80709"
    private val abcSha1 = "a9993e364706816aba3e25717850c26c9cd0d89d"

    // ==================== crypto(String, algorithm) ====================

    @Test
    fun `md5 of empty string should match known vector`() {
        assertThat(DigestUtils.crypto("", DigestUtils.MD5), `is`(emptyMd5))
    }

    @Test
    fun `md5 of abc should match known vector`() {
        assertThat(DigestUtils.crypto("abc", DigestUtils.MD5), `is`(abcMd5))
    }

    @Test
    fun `sha1 of empty string should match known vector`() {
        assertThat(DigestUtils.crypto("", DigestUtils.SHA1), `is`(emptySha1))
    }

    @Test
    fun `sha1 of abc should match known vector`() {
        assertThat(DigestUtils.crypto("abc", DigestUtils.SHA1), `is`(abcSha1))
    }

    @Test
    fun `crypto output should be lowercase hex`() {
        val hash = DigestUtils.crypto("hello", DigestUtils.MD5)
        assertThat(hash, `is`(hash.lowercase()))
        assertThat(hash.all { it.isDigit() || it in 'a'..'f' }, `is`(true))
    }

    // ==================== crypto(ByteArray, algorithm) ====================

    @Test
    fun `md5 of ByteArray overload should equal String overload`() {
        val bytes = "abc".toByteArray()
        assertThat(DigestUtils.crypto(bytes, DigestUtils.MD5), `is`(abcMd5))
    }

    // ==================== crypto2capital ====================

    @Test
    fun `crypto2capital should be uppercase colon-separated`() {
        val result = DigestUtils.crypto2capital("abc".toByteArray(), DigestUtils.MD5)
        assertThat(result, `is`(abcMd5.chunked(2).joinToString(":").uppercase()))
    }

    @Test
    fun `crypto2capital should produce 16 md5 segments`() {
        // MD5 固定 16 字节 → 16 段
        val result = DigestUtils.crypto2capital(byteArrayOf(), DigestUtils.MD5)
        assertThat(result.split(":").size, `is`(16))
    }

    @Test
    fun `crypto2capital should produce 20 sha1 segments`() {
        // SHA-1 固定 20 字节 → 20 段
        val result = DigestUtils.crypto2capital(byteArrayOf(), DigestUtils.SHA1)
        assertThat(result.split(":").size, `is`(20))
    }

    @Test(expected = java.security.NoSuchAlgorithmException::class)
    fun `unknown algorithm should throw`() {
        DigestUtils.crypto("x", "NOT-A-REAL-ALGO")
    }
}
