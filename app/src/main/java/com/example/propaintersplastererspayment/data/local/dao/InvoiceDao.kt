package com.example.propaintersplastererspayment.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.propaintersplastererspayment.data.local.entity.InvoiceEntity
import com.example.propaintersplastererspayment.data.local.entity.InvoiceLineEntity
import com.example.propaintersplastererspayment.data.local.model.InvoiceWithLines
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {

    // ── Invoice queries ────────────────────────────────────────────────────

    @Query("SELECT * FROM invoices WHERE jobOwnerId = :jobId ORDER BY issueDate DESC")
    fun observeInvoicesForJob(jobId: Long): Flow<List<InvoiceEntity>>

    /**
     * Returns the single invoice linked to a job, or null if none exists yet.
     * Used by InvoiceViewModel to observe whether an invoice exists for this job.
     */
    @Query("SELECT * FROM invoices WHERE jobOwnerId = :jobId LIMIT 1")
    fun observeInvoiceForJob(jobId: Long): Flow<InvoiceEntity?>

    @Transaction
    @Query("SELECT * FROM invoices WHERE invoiceId = :invoiceId LIMIT 1")
    fun observeInvoiceWithLines(invoiceId: Long): Flow<InvoiceWithLines?>

    /**
     * Returns the total number of invoices in the database.
     * Used to generate the next invoice number (e.g. INV-0003).
     */
    @Query("SELECT COUNT(*) FROM invoices")
    suspend fun getInvoiceCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity): Long

    @Update
    suspend fun updateInvoice(invoice: InvoiceEntity)

    @Delete
    suspend fun deleteInvoice(invoice: InvoiceEntity)

    // ── Invoice line queries ───────────────────────────────────────────────

    @Query("SELECT * FROM invoice_lines WHERE invoiceOwnerId = :invoiceId ORDER BY sortOrder ASC")
    fun observeInvoiceLines(invoiceId: Long): Flow<List<InvoiceLineEntity>>

    /**
     * Fetches a single invoice line by its ID.
     * Used when the ViewModel needs to delete a specific line.
     */
    @Query("SELECT * FROM invoice_lines WHERE lineId = :lineId LIMIT 1")
    suspend fun getInvoiceLineById(lineId: Long): InvoiceLineEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceLine(line: InvoiceLineEntity): Long

    @Update
    suspend fun updateInvoiceLine(line: InvoiceLineEntity)

    @Delete
    suspend fun deleteInvoiceLine(line: InvoiceLineEntity)
}
