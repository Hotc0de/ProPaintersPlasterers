package com.example.propaintersplastererspayment.domain.repository

import com.example.propaintersplastererspayment.data.local.entity.InvoiceEntity
import com.example.propaintersplastererspayment.data.local.entity.InvoiceLineEntity
import com.example.propaintersplastererspayment.data.local.model.InvoiceWithLines
import kotlinx.coroutines.flow.Flow

interface InvoiceRepository {
    fun observeInvoicesForJob(jobId: Long): Flow<List<InvoiceEntity>>
    fun observeInvoiceForJob(jobId: Long): Flow<InvoiceEntity?>
    fun observeInvoiceWithLines(invoiceId: Long): Flow<InvoiceWithLines?>
    fun observeInvoiceLines(invoiceId: Long): Flow<List<InvoiceLineEntity>>
    suspend fun saveInvoice(invoice: InvoiceEntity): Long
    suspend fun saveInvoiceLine(line: InvoiceLineEntity): Long
    suspend fun deleteInvoice(invoice: InvoiceEntity)
    suspend fun deleteInvoiceLine(line: InvoiceLineEntity)
    suspend fun getInvoiceLine(lineId: Long): InvoiceLineEntity?
    /**
     * Generates an invoice number that is guaranteed to be unique in the database.
     * Format: PREFIX-ABC123456 (3 random uppercase letters + 6 random digits).
     * Retries until a unique value is found.
     */
    suspend fun generateUniqueInvoiceNumber(prefix: String): String
}
