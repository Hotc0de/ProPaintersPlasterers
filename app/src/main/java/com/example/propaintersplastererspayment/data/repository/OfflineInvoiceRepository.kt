package com.example.propaintersplastererspayment.data.repository

import com.example.propaintersplastererspayment.core.util.InvoiceNumberGenerator
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

    override fun observeInvoiceForJob(jobId: Long): Flow<InvoiceEntity?> =
        invoiceDao.observeInvoiceForJob(jobId)

    override fun observeInvoiceWithLines(invoiceId: Long): Flow<InvoiceWithLines?> =
        invoiceDao.observeInvoiceWithLines(invoiceId)

    override fun observeInvoiceLines(invoiceId: Long): Flow<List<InvoiceLineEntity>> =
        invoiceDao.observeInvoiceLines(invoiceId)

    override suspend fun saveInvoice(invoice: InvoiceEntity): Long {
        return if (invoice.invoiceId == 0L) {
            invoiceDao.insertInvoice(invoice)
        } else {
            invoiceDao.updateInvoice(invoice)
            invoice.invoiceId
        }
    }

    override suspend fun saveInvoiceLine(line: InvoiceLineEntity): Long {
        return if (line.lineId == 0L) {
            invoiceDao.insertInvoiceLine(line)
        } else {
            invoiceDao.updateInvoiceLine(line)
            line.lineId
        }
    }

    override suspend fun deleteInvoice(invoice: InvoiceEntity) =
        invoiceDao.deleteInvoice(invoice)

    override suspend fun deleteInvoiceLine(line: InvoiceLineEntity) =
        invoiceDao.deleteInvoiceLine(line)

    override suspend fun getInvoiceLine(lineId: Long): InvoiceLineEntity? =
        invoiceDao.getInvoiceLineById(lineId)

    /**
     * Generates a random invoice number in the format PREFIX-ABC123456 that is
     * guaranteed not to already exist in the database. Retries until unique.
     */
    override suspend fun generateUniqueInvoiceNumber(prefix: String): String {
        var number: String
        do {
            number = InvoiceNumberGenerator.generate(prefix)
        } while (invoiceDao.invoiceNumberExists(number) > 0)
        return number
    }
}

