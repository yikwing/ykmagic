package com.yikwing.extension.date

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAccessor

/**
 * Java 8 时间 API 扩展函数
 *
 * 提供更符合 Kotlin 习惯的日期时间操作
 */

// ========== 常用格式化器（避免重复创建） ==========

private val DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss")

// ========== LocalDateTime 扩展 ==========

/**
 * 格式化为标准日期时间字符串
 * @return 格式: yyyy-MM-dd HH:mm:ss
 *
 * 示例:
 * ```kotlin
 * LocalDateTime.now().formatDateTime()  // "2024-12-10 14:30:00"
 * ```
 */
fun LocalDateTime.formatDateTime(): String = DATETIME_FORMATTER.format(this)

/**
 * 使用自定义格式格式化
 * @param pattern 日期时间格式，例如 "yyyy/MM/dd HH:mm"
 *
 * 示例:
 * ```kotlin
 * LocalDateTime.now().format("yyyy年MM月dd日")  // "2024年12月10日"
 * ```
 */
fun LocalDateTime.format(pattern: String): String = DateTimeFormatter.ofPattern(pattern).format(this)

/**
 * 获取当前日期时间字符串
 * @return 格式: yyyy-MM-dd HH:mm:ss
 */
fun nowDateTime(): String = LocalDateTime.now().formatDateTime()

// ========== LocalDate 扩展 ==========

/**
 * 格式化为标准日期字符串
 * @return 格式: yyyy-MM-dd
 *
 * 示例:
 * ```kotlin
 * LocalDate.now().formatDate()  // "2024-12-10"
 * ```
 */
fun LocalDate.formatDate(): String = DATE_FORMATTER.format(this)

/**
 * 使用自定义格式格式化
 * @param pattern 日期格式，例如 "yyyy/MM/dd"
 *
 * 示例:
 * ```kotlin
 * LocalDate.now().format("yyyy年MM月dd日")  // "2024年12月10日"
 * ```
 */
fun LocalDate.format(pattern: String): String = DateTimeFormatter.ofPattern(pattern).format(this)

/**
 * 解析日期字符串
 * @param pattern 日期格式，默认 "yyyy-MM-dd"
 *
 * 示例:
 * ```kotlin
 * "2024-12-10".toLocalDate()  // LocalDate(2024, 12, 10)
 * "2024/12/10".toLocalDate("yyyy/MM/dd")
 * ```
 */
fun String.toLocalDate(pattern: String = "yyyy-MM-dd"): LocalDate = LocalDate.parse(this, DateTimeFormatter.ofPattern(pattern))

/**
 * 获取当前日期字符串
 * @return 格式: yyyy-MM-dd
 */
fun nowDate(): String = LocalDate.now().formatDate()

/**
 * 计算两个日期之间的天数差
 *
 * 示例:
 * ```kotlin
 * val start = LocalDate.of(2024, 1, 1)
 * val end = LocalDate.of(2024, 12, 31)
 * start.daysBetween(end)  // 365
 * ```
 */
fun LocalDate.daysBetween(other: LocalDate): Long = ChronoUnit.DAYS.between(this, other)

/**
 * 判断是否是今天
 */
fun LocalDate.isToday(): Boolean = this == LocalDate.now()

/**
 * 判断是否是过去的日期
 */
fun LocalDate.isPast(): Boolean = this < LocalDate.now()

/**
 * 判断是否是未来的日期
 */
fun LocalDate.isFuture(): Boolean = this > LocalDate.now()

// ========== LocalTime 扩展 ==========

/**
 * 格式化为标准时间字符串
 * @return 格式: HH:mm:ss
 *
 * 示例:
 * ```kotlin
 * LocalTime.now().formatTime()  // "14:30:00"
 * ```
 */
fun LocalTime.formatTime(): String = TIME_FORMATTER.format(this)

/**
 * 使用自定义格式格式化
 * @param pattern 时间格式，例如 "HH:mm"
 *
 * 示例:
 * ```kotlin
 * LocalTime.now().format("HH:mm")  // "14:30"
 * ```
 */
fun LocalTime.format(pattern: String): String = DateTimeFormatter.ofPattern(pattern).format(this)

/**
 * 获取当前时间字符串
 * @return 格式: HH:mm:ss
 */
fun nowTime(): String = LocalTime.now().formatTime()

// ========== 通用扩展 ==========

/**
 * 格式化任意 TemporalAccessor
 * @param pattern 格式模式
 */
fun TemporalAccessor.format(pattern: String): String = DateTimeFormatter.ofPattern(pattern).format(this)
