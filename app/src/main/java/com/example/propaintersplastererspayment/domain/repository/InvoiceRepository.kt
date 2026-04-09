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
    suspend fun getNextInvoiceNumber(prefix: String): String
}
