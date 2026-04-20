package com.example.propaintersplastererspayment.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.propaintersplastererspayment.data.local.entity.JobPaintEntity
import com.example.propaintersplastererspayment.data.local.entity.SurfaceEntity

data class SurfaceWithJobPaint(
    @Embedded val surface: SurfaceEntity,
    @Relation(
        parentColumn = "selectedJobPaintId",
        entityColumn = "jobPaintId"
    )
    val jobPaint: JobPaintEntity?
)
