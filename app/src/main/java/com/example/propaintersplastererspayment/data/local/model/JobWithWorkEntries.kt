package com.example.propaintersplastererspayment.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.propaintersplastererspayment.data.local.entity.JobEntity
import com.example.propaintersplastererspayment.data.local.entity.WorkEntryEntity

data class JobWithWorkEntries(
    @Embedded val job: JobEntity,
    @Relation(
        entity = WorkEntryEntity::class,
        parentColumn = "jobId",
        entityColumn = "jobOwnerId"
    )
    val workEntries: List<WorkEntryEntity>
)

