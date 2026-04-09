package com.example.propaintersplastererspayment.core.util

/**
 * Generates random invoice numbers in the format INV-ABC123456.
 * Uniqueness is enforced by repository/database checks.
 */
object InvoiceNumberGenerator {

    private val LETTERS = ('A'..'Z').toList()
    private val DIGITS = ('0'..'9').toList()

    /** Returns a candidate like INV-XBR047291. */
    fun generate(): String {
        val letters = (1..3).map { LETTERS.random() }.joinToString("")
        val digits = (1..6).map { DIGITS.random() }.joinToString("")
        return "INV-$letters$digits"
    }
}

