package com.example.propaintersplastererspayment.data.repository

import com.example.propaintersplastererspayment.data.local.dao.AccessDao
import com.example.propaintersplastererspayment.data.local.entity.AccessItemEntity
import com.example.propaintersplastererspayment.domain.repository.AccessRepository
import kotlinx.coroutines.flow.Flow

class OfflineAccessRepository(
    private val accessDao: AccessDao
) : AccessRepository {
    override fun observeAccessItems(jobId: Long): Flow<List<AccessItemEntity>> = 
        accessDao.observeAccessItems(jobId)

    override suspend fun saveAccessItem(item: AccessItemEntity): Long {
        return accessDao.saveAccessItemAndSyncProperty(item)
    }

    override suspend fun deleteAccessItem(item: AccessItemEntity) {
        accessDao.deleteAccessItemAndSyncProperty(item)
    }
}
