package com.example.propaintersplastererspayment.core.util

object SettingsPhoneFormatUtils {
    private const val MIN_DIGITS = 9
    private const val MAX_DIGITS = 10
    private val PHONE_REGEX = Regex("^\\d{3}-\\d{3}-\\d{3,4}$")

    fun formatInput(raw: String): String {
        val digits = MaskedInputUtils.digitsOnly(raw).take(MAX_DIGITS)
        if (digits.isEmpty()) return ""

        val first = digits.take(3)
        val second = digits.drop(3).take(3)
        val third = digits.drop(6)

        return buildString {
            append(first)
            if (second.isNotEmpty()) {
                append('-')
                append(second)
            }
            if (third.isNotEmpty()) {
                append('-')
                append(third)
            }
        }
    }

    fun isValid(formatted: String): Boolean {
        val digits = MaskedInputUtils.digitsOnly(formatted)
        return digits.length in MIN_DIGITS..MAX_DIGITS && PHONE_REGEX.matches(formatInput(formatted))
    }
}

