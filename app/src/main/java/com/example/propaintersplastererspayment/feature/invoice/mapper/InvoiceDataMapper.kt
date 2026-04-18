package com.example.propaintersplastererspayment.feature.invoice.mapper

import com.example.propaintersplastererspayment.core.util.DateFormatUtils
import com.example.propaintersplastererspayment.core.pdf.InvoicePdfData
import com.example.propaintersplastererspayment.feature.invoice.ui.luxury.InvoiceData
import com.example.propaintersplastererspayment.feature.invoice.ui.luxury.InvoiceLineItem
import java.util.Calendar

object InvoiceDataMapper {

    fun map(pdfData: InvoicePdfData): InvoiceData {
        return InvoiceData(
            invoiceNumber = pdfData.invoiceNumber,
            issueDate = DateFormatUtils.formatDisplayDate(pdfData.issueDate),
            dueDate = pdfData.dueDate?.let { DateFormatUtils.formatDisplayDate(it) },
            billTo = pdfData.billToName,
            jobAddress = pdfData.jobAddress,
            lineItems = pdfData.lines.map { line ->
                InvoiceLineItem(
                    description = line.description,
                    quantity = line.qty.toInt().coerceAtLeast(1),
                    rate = line.rate,
                    amount = line.amount
                )
            },
            subtotal = pdfData.subtotalExGst,
            includeGst = pdfData.includeGst,
            gstRate = pdfData.gstRate,
            gstAmount = pdfData.gstAmount,
            total = pdfData.finalTotal,
            businessName = pdfData.business.businessName.ifBlank { "Pro Painters" },
            businessSubtitle = "& PLASTERERS",
            businessAddress = pdfData.business.address,
            businessPhone = pdfData.business.phoneNumber,
            businessEmail = pdfData.business.email,
            accountNumber = pdfData.business.bankAccountNumber
        )
    }

    private fun calculateDueDate(displayIssueDate: String): String {
        val issueDate = DateFormatUtils.parseDisplayDate(displayIssueDate) ?: return displayIssueDate
        val calendar = Calendar.getInstance().apply {
            time = issueDate
            add(Calendar.DAY_OF_YEAR, 10)
        }
        return DateFormatUtils.formatTimestampToDisplay(calendar.timeInMillis)
    }
}

