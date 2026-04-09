package com.example.propaintersplastererspayment.data.repository

import com.example.propaintersplastererspayment.core.util.InvoiceUtils
import com.example.propaintersplastererspayment.data.local.dao.InvoiceDao
import com.example.propaintersplastererspayment.data.local.entity.InvoiceEntity
import com.example.propaintersplastererspayment.data.local.entity.InvoiceLineEntity
import com.example.propaintersplastererspayment.data.local.model.InvoiceWithLines
import com.example.propaintersplastererspayment.domain.repository.InvoiceRepository
import kotlinx.coroutines.flow.Flow

/**
 * Concrete implementation of [InvoiceRepository] that reads from and writes to the local
 * Room database via [InvoiceDao]. All database access is off the main thread via suspend
 * functions or Flows.
 */
class OfflineInvoiceRepository(
    private val invoiceDao: InvoiceDao
) : InvoiceRepository {

    override fun observeInvoicesForJob(jobId: Long): Flow<List<InvoiceEntity>> =
        invoiceDao.observeInvoicesForJob(jobId)

    // Returns the single invoice for a job (null if none has been created yet)
    override fun observeInvoiceForJob(jobId: Long): Flow<InvoiceEntity?> =
        invoiceDao.observeInvoiceForJob(jobId)

    override fun observeInvoiceWithLines(invoiceId: Long): Flow<InvoiceWithLines?> =
        invoiceDao.observeInvoiceWithLines(invoiceId)

    override fun observeInvoiceLines(invoiceId: Long): Flow<List<InvoiceLineEntity>> =
        invoiceDao.observeInvoiceLines(invoiceId)

    /**
     * Inserts a new invoice (invoiceId == 0) or updates an existing one.
     * Returns the final invoiceId in both cases.
     */
    override suspend fun saveInvoice(invoice: InvoiceEntity): Long {
        return if (invoice.invoiceId == 0L) {
            invoiceDao.insertInvoice(invoice)
        } else {
            invoiceDao.updateInvoice(invoice)
            invoice.invoiceId
        }
    }

    /**
     * Inserts a new invoice line (lineId == 0) or updates an existing one.
     * Returns the final lineId in both cases.
     */
    override suspend fun saveInvoiceLine(line: InvoiceLineEntity): Long {
        return if (line.lineId == 0L) {
            invoiceDao.insertInvoiceLine(line)
        } else {
            invoiceDao.updateInvoiceLine(line)
            line.lineId
        }
    }

    // Deleting an invoice also deletes all its lines via the CASCADE foreign key rule
    override suspend fun deleteInvoice(invoice: InvoiceEntity) =
        invoiceDao.deleteInvoice(invoice)

    override suspend fun deleteInvoiceLine(line: InvoiceLineEntity) =
        invoiceDao.deleteInvoiceLine(line)

    override suspend fun getInvoiceLine(lineId: Long): InvoiceLineEntity? =
        invoiceDao.getInvoiceLineById(lineId)

    /**
     * Generates the next invoice number based on how many invoices are stored.
     * Example: if there are 5 invoices, the next number is "INV-0006".
     */
    override suspend fun getNextInvoiceNumber(prefix: String): String {
        val count = invoiceDao.getInvoiceCount()
        return InvoiceUtils.generateInvoiceNumber(count, prefix)
    }
}
