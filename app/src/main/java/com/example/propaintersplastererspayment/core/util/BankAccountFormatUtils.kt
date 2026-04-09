package com.example.propaintersplastererspayment.core.util

object BankAccountFormatUtils {
    private val SPEC = MaskSpec(groups = listOf(2, 4, 7, 2))
    private val ACCOUNT_REGEX = MaskedInputUtils.buildRegex(SPEC)

    fun formatInput(raw: String): String = MaskedInputUtils.formatInput(raw, SPEC)

    fun isValid(formatted: String): Boolean = ACCOUNT_REGEX.matches(formatted)

    fun isComplete(formatted: String): Boolean = MaskedInputUtils.isComplete(formatted, SPEC)
}

