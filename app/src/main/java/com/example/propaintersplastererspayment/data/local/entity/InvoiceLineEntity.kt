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
            childColumns = ["invoiceOwnerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["invoiceOwnerId"])]
)
data class InvoiceLineEntity(
    @PrimaryKey(autoGenerate = true)
    val lineId: Long = 0,
    val invoiceOwnerId: Long,
    val description: String,
    val qty: Double,
    val rate: Double,
    val amount: Double,
    val isManualAmount: Boolean,
    val sortOrder: Int
)

