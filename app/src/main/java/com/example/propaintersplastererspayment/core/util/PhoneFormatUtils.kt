package com.example.propaintersplastererspayment.core.util

object PhoneFormatUtils {
    private val SPEC = MaskSpec(groups = listOf(3, 7))
    private val PHONE_REGEX = MaskedInputUtils.buildRegex(SPEC)

    fun formatInput(raw: String): String = MaskedInputUtils.formatInput(raw, SPEC)

    fun isValid(formatted: String): Boolean = PHONE_REGEX.matches(formatted)

    fun isComplete(formatted: String): Boolean = MaskedInputUtils.isComplete(formatted, SPEC)
}

