package com.example.propaintersplastererspayment.data.local.model

import androidx.room.Embedded
import com.example.propaintersplastererspayment.data.local.entity.SurfaceEntity

data class SurfaceWithJobPaint(
    @Embedded val surface: SurfaceEntity,
    
    // Undercoat Details
    val undercoatPaintId: Long? = null,
    val undercoatBrandName: String? = null,
    val undercoatPaintName: String? = null,
    val undercoatHexCode: String? = null,

    // Maincoat Details
    val maincoatPaintId: Long? = null,
    val maincoatBrandName: String? = null,
    val maincoatPaintName: String? = null,
    val maincoatHexCode: String? = null
)
