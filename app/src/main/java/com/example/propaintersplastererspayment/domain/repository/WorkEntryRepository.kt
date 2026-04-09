package com.example.propaintersplastererspayment.domain.repository

import com.example.propaintersplastererspayment.data.local.entity.WorkEntryEntity
import kotlinx.coroutines.flow.Flow

interface WorkEntryRepository {
    fun observeEntriesForJob(jobId: Long): Flow<List<WorkEntryEntity>>
    fun observeTotalHoursForJob(jobId: Long): Flow<Double>
    suspend fun getEntry(entryId: Long): WorkEntryEntity?
    suspend fun saveEntry(entry: WorkEntryEntity): Long
    suspend fun deleteEntry(entry: WorkEntryEntity)
}


