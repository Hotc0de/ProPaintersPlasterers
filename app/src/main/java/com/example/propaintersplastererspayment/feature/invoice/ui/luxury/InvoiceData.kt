package com.example.propaintersplastererspayment.feature.invoice.ui.luxury

data class InvoiceData(
    val invoiceNumber: String,
    val issueDate: String,
    val dueDate: String? = null,
    val billTo: String,
    val billToAddress: String = "",
    val projectName: String = "",
    val projectDescription: String = "",
    val lineItems: List<InvoiceLineItem>,
    val subtotal: Double,
    val includeGst: Boolean = true,
    val gstRate: Double = 0.15,
    val gstAmount: Double = 0.0,
    val total: Double = 0.0,
    val businessName: String = "Pro Painters",
    val businessSubtitle: String = "Plasterers",
    val businessAddress: String = "170 Tancred Street",
    val businessPhone: String = "022-10701719",
    val businessEmail: String = "painters@gmail.com",
    val accountNumber: String = "22-2222-2222222-22",
    val bankName: String = "ANZ Bank"
)

data class InvoiceLineItem(
    val description: String,
    val quantity: Int,
    val rate: Double,
    val amount: Double,
    val isLabour: Boolean = false
)

