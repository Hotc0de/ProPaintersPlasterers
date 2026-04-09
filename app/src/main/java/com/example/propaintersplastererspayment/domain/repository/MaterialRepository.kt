package com.example.propaintersplastererspayment.domain.repository

import com.example.propaintersplastererspayment.data.local.entity.MaterialItemEntity
import kotlinx.coroutines.flow.Flow

interface MaterialRepository {
    fun observeMaterialsForJob(jobId: Long): Flow<List<MaterialItemEntity>>
    fun observeTotalMaterialCostForJob(jobId: Long): Flow<Double>
    suspend fun getMaterial(materialId: Long): MaterialItemEntity?
    suspend fun saveMaterial(item: MaterialItemEntity): Long
    suspend fun deleteMaterial(item: MaterialItemEntity)
}


