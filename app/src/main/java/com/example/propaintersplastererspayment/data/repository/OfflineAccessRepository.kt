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
        return if (item.accessId == 0L) {
            accessDao.insertAccessItem(item)
        } else {
            accessDao.updateAccessItem(item)
            item.accessId
        }
    }

    override suspend fun deleteAccessItem(item: AccessItemEntity) {
        accessDao.deleteAccessItem(item)
    }
}
