package com.example.propaintersplastererspayment.feature.invoice.ui.luxury

data class InvoiceData(
    val invoiceNumber: String,
    val issueDate: String,
    val dueDate: String? = null,
    val billTo: String,
    val jobAddress: String,
    val lineItems: List<InvoiceLineItem>,
    val subtotal: Double,
    val includeGst: Boolean = true,
    val gstRate: Double = 0.10,
    val gstAmount: Double = 0.0,
    val total: Double = 0.0,
    val businessName: String = "Pro Painters",
    val businessSubtitle: String = "& PLASTERERS",
    val businessAddress: String = "123 Craftsman Avenue\nSydney NSW 2000",
    val businessPhone: String = "+61 2 9876 5432",
    val businessEmail: String = "hello@elitepainting.com.au",
    val accountNumber: String = "01-1234-5678901-02"
)

data class InvoiceLineItem(
    val description: String,
    val quantity: Int,
    val rate: Double,
    val amount: Double
)

