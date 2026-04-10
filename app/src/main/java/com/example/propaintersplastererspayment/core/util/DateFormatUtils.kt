package com.example.propaintersplastererspayment.core.util

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Central place for converting between stored dates and user-facing display dates.
 *
 * Storage format: yyyy-MM-dd
 * Display format: dd-MM-yyyy
 */
object DateFormatUtils {
    private const val STORED_PATTERN = "yyyy-MM-dd"
    private const val DISPLAY_PATTERN = "dd-MM-yyyy"

    private fun formatter(pattern: String): SimpleDateFormat =
        SimpleDateFormat(pattern, Locale.getDefault()).apply {
            isLenient = false
        }

    fun todayDisplayDate(): String = formatter(DISPLAY_PATTERN).format(Date())

    fun formatDisplayDate(value: String): String {
        if (value.isBlank()) return value

        parseStoredDate(value)?.let { return formatter(DISPLAY_PATTERN).format(it) }
        parseDisplayDate(value)?.let { return formatter(DISPLAY_PATTERN).format(it) }
        return value
    }

    fun formatTimestampToDisplay(epochMillis: Long): String =
        formatter(DISPLAY_PATTERN).format(Date(epochMillis))

    fun parseStoredDate(value: String): Date? = parse(value, STORED_PATTERN)

    fun parseDisplayDate(value: String): Date? = parse(value, DISPLAY_PATTERN)

    fun toStoredDate(value: String): String? {
        if (value.isBlank()) return null

        parseDisplayDate(value)?.let { return formatter(STORED_PATTERN).format(it) }
        parseStoredDate(value)?.let { return formatter(STORED_PATTERN).format(it) }
        return null
    }

    fun isValidDisplayDate(value: String): Boolean = toStoredDate(value) != null

    private fun parse(value: String, pattern: String): Date? {
        return try {
            formatter(pattern).parse(value)
        } catch (_: ParseException) {
            null
        }
    }
}

