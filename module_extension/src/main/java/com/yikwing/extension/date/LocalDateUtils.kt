package com.yikwing.extension.date

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

/**
 * @Author yikwing
 * @Date 2024-09-28 23:17
 * @Description: Java 8 新时间 API 工具
 */

object LocalDateUtils {
    /**
     * 显示年月日时分秒，例如 2015-08-11 09:51:53.
     */
    private const val DATETIME_PATTERN: String = "yyyy-MM-dd HH:mm:ss"

    /**
     * 仅显示年月日，例如 2015-08-11.
     */
    private const val DATE_PATTERN: String = "yyyy-MM-dd"

    /**
     * 仅显示时分秒，例如 09:51:53.
     */
    private const val TIME_PATTERN: String = "HH:mm:ss"

    /**
     * 获取当前日期和时间字符串.
     *
     * @return String 日期时间字符串，例如 2015-08-11 09:51:53
     */
    fun getLocalDateTimeStr(): String = format(LocalDateTime.now(), DATETIME_PATTERN)

    /**
     * 获取当前日期字符串.
     *
     * @return String 日期字符串，例如2015-08-11
     */
    fun getLocalDateStr(): String = format(LocalDate.now(), DATE_PATTERN)

    /**
     * 获取当前时间字符串.
     *
     * @return String 时间字符串，例如 09:51:53
     */
    fun getLocalTimeStr(): String = format(LocalTime.now(), TIME_PATTERN)

    /**
     * 获取日期时间字符串
     *
     * @param temporal 需要转化的日期时间
     * @param pattern  时间格式
     * @return String 日期时间字符串，例如 2015-08-11 09:51:53
     */
    private fun format(
        temporal: TemporalAccessor,
        pattern: String,
    ): String {
        val dateTimeFormatter = DateTimeFormatter.ofPattern(pattern)
        return dateTimeFormatter.format(temporal)
    }

    /**
     * 解析日期
     *
     * @param dateStr  需要解析的日期字符串
     * @param pattern  日期格式，例如yyyy-MM-dd
     * @return LocalDate
     */
    fun parseDate(
        dateStr: String,
        pattern: String = DATE_PATTERN,
    ): LocalDate {
        val formatter = DateTimeFormatter.ofPattern(pattern)
        return LocalDate.parse(dateStr, formatter)
    }
}
