package com.example.propaintersplastererspayment.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "invoice_lines",
    foreignKeys = [
        ForeignKey(
            entity = InvoiceEntity::class,
            parentColumns = ["invoiceId"],
            childColumns = ["invoiceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["invoiceId"])]
)
data class InvoiceLineEntity(
    @PrimaryKey(autoGenerate = true)
    val invoiceLineId: Long = 0,
    val invoiceId: Long,
    val description: String,
    val qty: Double,
    val rate: Double,
    val amount: Double,
    val manualAmountOverride: Boolean,
    val sortOrder: Int
) {
    // Backward-compatible aliases used by existing UI/viewmodel code.
    val lineId: Long get() = invoiceLineId
    val invoiceOwnerId: Long get() = invoiceId
    val isManualAmount: Boolean get() = manualAmountOverride
}

