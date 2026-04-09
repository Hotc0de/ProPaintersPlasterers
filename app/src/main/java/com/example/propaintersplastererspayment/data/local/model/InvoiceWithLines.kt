package com.example.propaintersplastererspayment.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.propaintersplastererspayment.data.local.entity.InvoiceEntity
import com.example.propaintersplastererspayment.data.local.entity.InvoiceLineEntity

data class InvoiceWithLines(
    @Embedded val invoice: InvoiceEntity,
    @Relation(
        entity = InvoiceLineEntity::class,
        parentColumn = "invoiceId",
        entityColumn = "invoiceId"
    )
    val lines: List<InvoiceLineEntity>
)

