package com.example.propaintersplastererspayment.core.util

data class MaskSpec(
    val groups: List<Int>,
    val separator: Char = '-'
) {
    val totalDigits: Int = groups.sum()
}

object MaskedInputUtils {

    fun digitsOnly(value: String): String = value.filter(Char::isDigit)

    /**
     * Applies a fixed digit mask and keeps separators at the correct positions.
     * Example for [2,4,7,2]: 121234123456712 -> 12-1234-1234567-12
     */
    fun formatInput(raw: String, spec: MaskSpec): String {
        val digits = digitsOnly(raw).take(spec.totalDigits)
        val out = StringBuilder()
        var cursor = 0

        spec.groups.forEachIndexed { index, groupSize ->
            if (cursor >= digits.length) return@forEachIndexed
            val end = (cursor + groupSize).coerceAtMost(digits.length)
            if (index > 0) out.append(spec.separator)
            out.append(digits.substring(cursor, end))
            cursor = end
        }

        return out.toString()
    }

    fun isComplete(formatted: String, spec: MaskSpec): Boolean {
        val digits = digitsOnly(formatted)
        return digits.length == spec.totalDigits
    }

    fun buildRegex(spec: MaskSpec): Regex {
        val body = spec.groups.joinToString("\\${spec.separator}") { "\\d{$it}" }
        return Regex("^$body$")
    }
}

