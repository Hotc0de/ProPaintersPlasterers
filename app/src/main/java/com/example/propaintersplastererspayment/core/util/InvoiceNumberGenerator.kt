package com.example.propaintersplastererspayment.core.util

/**
 * Generates random invoice numbers in the format: PREFIX-ABC123456
 *
 * Format breakdown:
 *  - PREFIX  : comes from Business Settings (default "INV")
 *  - ABC     : 3 random uppercase letters
 *  - 123456  : 6 random digits
 *
 * Example: INV-XBR047291
 *
 * Uniqueness is NOT guaranteed here — callers must loop until a unique number
 * is found by checking the database (see [OfflineInvoiceRepository]).
 */
object InvoiceNumberGenerator {

    private val LETTERS = ('A'..'Z').toList()
    private val DIGITS = ('0'..'9').toList()

    /**
     * Generates one candidate invoice number.
     * @param prefix The invoice prefix from Business Settings (e.g. "INV", "PP", "INVOICE").
     *               A trailing dash is added automatically if not already present.
     * @return A string like "INV-XBR047291"
     */
    fun generate(prefix: String): String {
        val cleanPrefix = prefix.trim().ifBlank { "INV" }.trimEnd('-')
        val letters = (1..3).map { LETTERS.random() }.joinToString("")
        val digits = (1..6).map { DIGITS.random() }.joinToString("")
        return "$cleanPrefix-$letters$digits"
    }
}

