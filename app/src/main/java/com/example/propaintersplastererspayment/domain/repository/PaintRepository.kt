package com.example.propaintersplastererspayment.domain.repository

import com.example.propaintersplastererspayment.data.local.dao.JobPaintDisplay
import com.example.propaintersplastererspayment.data.local.entity.JobPaintEntity
import com.example.propaintersplastererspayment.data.local.entity.PaintBrandEntity
import com.example.propaintersplastererspayment.data.local.entity.PaintItemEntity
import kotlinx.coroutines.flow.Flow

interface PaintRepository {
    // Brands
    fun getAllBrandsStream(): Flow<List<PaintBrandEntity>>
    suspend fun insertBrand(brand: PaintBrandEntity): Long
    suspend fun deleteBrand(brand: PaintBrandEntity)

    // Paint Items
    fun getPaintsForBrandStream(brandId: Long): Flow<List<PaintItemEntity>>
    fun getPaintByIdStream(paintId: Long): Flow<PaintItemEntity?>
    fun getAllPaintsWithBrandStream(): Flow<List<PaintItemEntity>>
    suspend fun insertPaint(paint: PaintItemEntity): Long
    suspend fun updatePaint(paint: PaintItemEntity)
    suspend fun deletePaint(paint: PaintItemEntity)

    // Job Mappings
    fun getPaintsForJobStream(jobId: Long): Flow<List<JobPaintDisplay>>
    suspend fun addPaintToJob(jobPaint: JobPaintEntity)
    suspend fun removePaintFromJob(jobPaintId: Long)
}
