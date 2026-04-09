package com.example.propaintersplastererspayment.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.propaintersplastererspayment.data.local.entity.InvoiceEntity
import com.example.propaintersplastererspayment.data.local.entity.JobEntity

data class JobWithInvoices(
    @Embedded val job: JobEntity,
    @Relation(
        entity = InvoiceEntity::class,
        parentColumn = "jobId",
        entityColumn = "jobId"
    )
    val invoices: List<InvoiceEntity>
)

