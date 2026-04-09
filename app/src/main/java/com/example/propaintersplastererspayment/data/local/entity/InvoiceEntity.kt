package com.example.propaintersplastererspayment.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "invoices",
    foreignKeys = [
        ForeignKey(
            entity = JobEntity::class,
            parentColumns = ["jobId"],
            childColumns = ["jobId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["clientId"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["jobId"]),
        Index(value = ["clientId"]),
        Index(value = ["invoiceNumber"], unique = true)
    ]
)
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true)
    val invoiceId: Long = 0,
    val invoiceNumber: String,
    val jobId: Long,
    val clientId: Long? = null,
    val invoiceDate: String,
    val billToNameSnapshot: String,
    val billToAddressSnapshot: String,
    val billToPhoneSnapshot: String,
    val billToEmailSnapshot: String,
    val subtotalExclusiveGst: Double,
    val gstEnabled: Boolean,
    val gstRate: Double,
    val gstAmount: Double,
    val otherAmount: Double = 0.0,
    val totalAmount: Double,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    // Backward-compatible aliases used by existing UI/viewmodel code.
    val billToName: String get() = billToNameSnapshot
    val issueDate: String get() = invoiceDate
    val includeGst: Boolean get() = gstEnabled
}

