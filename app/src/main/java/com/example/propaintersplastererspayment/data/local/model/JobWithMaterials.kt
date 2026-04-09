package com.example.propaintersplastererspayment.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.propaintersplastererspayment.data.local.entity.JobEntity
import com.example.propaintersplastererspayment.data.local.entity.MaterialItemEntity

data class JobWithMaterials(
    @Embedded val job: JobEntity,
    @Relation(
        entity = MaterialItemEntity::class,
        parentColumn = "jobId",
        entityColumn = "jobOwnerId"
    )
    val materials: List<MaterialItemEntity>
)

