package com.example.propaintersplastererspayment.core.util

/**
 * Utility functions shared by the Invoice ViewModel and the Invoice screen.
 *
 * Keeping these in a standalone object makes them easy to unit-test and keeps the
 * ViewModel lean.
 */
object InvoiceUtils {

    /** Default GST rate configured for this app: 15 %. */
    const val DEFAULT_GST_RATE = 0.15

    /**
     * Returns today's date as a string in the format expected by the invoice date field.
     * Example: "10-04-2026"
     */
    fun todayDate(): String =
        DateFormatUtils.todayDisplayDate()

    /**
     * Generates an invoice number in the format "INV-XXXX" where XXXX is zero-padded.
     *
     * @param existingCount  The number of invoices already stored. The new number will
     *                       be existingCount + 1, so the very first invoice is "INV-0001".
     *
     * Example: existingCount = 5  →  "INV-0006"
     */
    fun generateInvoiceNumber(existingCount: Int, prefix: String): String =
        "%s%04d".format(prefix, existingCount + 1)

    /**
     * Formats a GST rate (stored as a decimal) into a human-readable percentage string.
     * Example: 0.10  →  "10%"
     */
    fun formatGstRate(gstRate: Double): String =
        "${(gstRate * 100).toInt()}%"

    /**
     * Tries to parse a text string into a Double.
     * Accepts standard decimal format like "49.99" or "1234".
     * Returns null if the string is blank or not a valid number.
     */
    fun parseAmount(text: String): Double? =
        text.trim().replace(",", "").toDoubleOrNull()

    /** Sum all hours from timesheet entries for a single job. */
    fun calculateTotalLabourHours(hours: List<Double>): Double = hours.sum()

    /** Labour amount = total hours × labour rate. */
    fun calculateLabourCost(totalHours: Double, labourRate: Double): Double = totalHours * labourRate

    /** Sum all material prices for the current job. */
    fun calculateTotalMaterialCost(prices: List<Double>): Double = prices.sum()

    /**
     * Calculates a due date that is 10 working days from the given issue date.
     * Skips weekends (Saturday and Sunday).
     *
     * @param issueDate The starting date in "dd-MM-yyyy" format.
     * @return The due date in "dd-MM-yyyy" format, or null if issueDate is invalid.
     */
    fun calculateDefaultDueDate(issueDate: String): String? {
        val date = DateFormatUtils.parseDisplayDate(issueDate) ?: return null
        val calendar = java.util.Calendar.getInstance().apply {
            time = date
        }

        var workingDaysAdded = 0
        while (workingDaysAdded < 10) {
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
            val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)
            if (dayOfWeek != java.util.Calendar.SATURDAY && dayOfWeek != java.util.Calendar.SUNDAY) {
                workingDaysAdded++
            }
        }

        return DateFormatUtils.formatTimestampToDisplay(calendar.timeInMillis)
    }

    /**
     * Validates the invoice header form fields.
     * Returns an error message string, or null if everything is valid.
     */
    fun validateHeader(
        invoiceNumber: String,
        billToName: String,
        issueDate: String
    ): String? = when {
        invoiceNumber.isBlank() -> "Invoice number is required."
        billToName.isBlank()    -> "Bill To name is required."
        issueDate.isBlank()     -> "Invoice date is required."
        !DateFormatUtils.isValidDisplayDate(issueDate) -> "Use date format dd-MM-yyyy."
        else -> null
    }

    /**
     * Validates an invoice line form.
     * Returns an error message string, or null if everything is valid.
     */
    fun validateLine(
        description: String,
        qtyText: String,
        rateText: String,
        amountText: String,
        isManualAmount: Boolean
    ): String? = when {
        description.isBlank() -> "Description is required."
        isManualAmount && parseAmount(amountText) == null ->
            "Enter a valid amount."
        !isManualAmount && parseAmount(qtyText) == null ->
            "Enter a valid quantity."
        !isManualAmount && parseAmount(rateText) == null ->
            "Enter a valid rate."
        else -> null
    }
}

