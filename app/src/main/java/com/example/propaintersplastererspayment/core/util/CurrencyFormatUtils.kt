package com.example.propaintersplastererspayment.core.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatUtils {
    fun formatCurrency(amount: Double, locale: Locale = Locale.getDefault()): String {
        return NumberFormat.getCurrencyInstance(locale).format(amount)
    }
}

