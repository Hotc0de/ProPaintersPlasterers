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
            childColumns = ["jobOwnerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["clientId"],
            childColumns = ["clientOwnerId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["jobOwnerId"]),
        Index(value = ["clientOwnerId"]),
        Index(value = ["invoiceNumber"], unique = true)
    ]
)
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true)
    val invoiceId: Long = 0,
    val jobOwnerId: Long,
    val clientOwnerId: Long? = null,
    val invoiceNumber: String,
    val billToName: String,
    val issueDate: String,
    val includeGst: Boolean,
    val gstRate: Double = 0.1,
    val otherAmount: Double = 0.0,
    val notes: String = ""
)

