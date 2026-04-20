package com.example.propaintersplastererspayment.data.local.model

import androidx.room.Embedded
import com.example.propaintersplastererspayment.data.local.entity.SurfaceEntity

data class SurfaceWithJobPaint(
    @Embedded val surface: SurfaceEntity,
    val jobPaintId: Long? = null,
    val paintId: Long? = null,
    val brandName: String? = null,
    val paintName: String? = null,
    val paintCode: String? = null,
    val hexCode: String? = null,
    val jobPaintNotes: String? = null
)
