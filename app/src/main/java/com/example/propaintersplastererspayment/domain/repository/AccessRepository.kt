package com.example.propaintersplastererspayment.domain.repository

import com.example.propaintersplastererspayment.data.local.entity.AccessItemEntity
import kotlinx.coroutines.flow.Flow

interface AccessRepository {
    fun observeAccessItems(jobId: Long): Flow<List<AccessItemEntity>>
    suspend fun saveAccessItem(item: AccessItemEntity): Long
    suspend fun deleteAccessItem(item: AccessItemEntity)
}
