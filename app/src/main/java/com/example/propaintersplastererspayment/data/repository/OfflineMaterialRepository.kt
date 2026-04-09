package com.example.propaintersplastererspayment.data.repository

import com.example.propaintersplastererspayment.data.local.dao.MaterialDao
import com.example.propaintersplastererspayment.data.local.entity.MaterialItemEntity
import com.example.propaintersplastererspayment.domain.repository.MaterialRepository
import kotlinx.coroutines.flow.Flow

class OfflineMaterialRepository(
    private val materialDao: MaterialDao
) : MaterialRepository {
    override fun observeMaterialsForJob(jobId: Long): Flow<List<MaterialItemEntity>> =
        materialDao.observeMaterialsForJob(jobId)

    override fun observeTotalMaterialCostForJob(jobId: Long): Flow<Double> =
        materialDao.observeTotalMaterialCostForJob(jobId)

    override suspend fun getMaterial(materialId: Long): MaterialItemEntity? =
        materialDao.getMaterialById(materialId)

    override suspend fun saveMaterial(item: MaterialItemEntity): Long {
        return if (item.materialId == 0L) {
            materialDao.insertMaterial(item)
        } else {
            materialDao.updateMaterial(item)
            item.materialId
        }
    }

    override suspend fun deleteMaterial(item: MaterialItemEntity) {
        materialDao.deleteMaterial(item)
    }
}


