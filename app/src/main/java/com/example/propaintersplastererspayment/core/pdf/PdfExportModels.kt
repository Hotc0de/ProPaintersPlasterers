package com.example.propaintersplastererspayment.core.pdf

data class PdfBusinessDetails(
    val businessName: String,
    val address: String,
    val phoneNumber: String,
    val email: String,
    val gstNumber: String,
    val bankAccountNumber: String,
    val bankName: String
)

data class WorkEntryPdfRow(
    val workDate: String,
    val workerName: String,
    val startTime: String,
    val finishTime: String,
    val hoursWorked: Double
)

data class MaterialPdfRow(
    val materialName: String,
    val price: Double
)

data class TimesheetPdfData(
    val fileName: String,
    val exportedAt: String,
    val business: PdfBusinessDetails,
    val jobName: String,
    val jobAddress: String,
    val workEntries: List<WorkEntryPdfRow>,
    val totalHours: Double,
    val materials: List<MaterialPdfRow>,
    val totalMaterialCost: Double
)

data class InvoiceLinePdfRow(
    val description: String,
    val qty: Double,
    val rate: Double,
    val amount: Double,
    val isManualAmount: Boolean
)

data class InvoicePdfData(
    val fileName: String,
    val exportedAt: String,
    val business: PdfBusinessDetails,
    val jobName: String,
    val jobAddress: String,
    val invoiceNumber: String,
    val issueDate: String,
    val dueDate: String? = null,
    val billToName: String,
    val lines: List<InvoiceLinePdfRow>,
    val subtotalExGst: Double,
    val includeGst: Boolean,
    val gstRate: Double,
    val gstAmount: Double,
    val totalIncGst: Double,
    val finalTotal: Double,
    val notes: String
)

