package com.example.propaintersplastererspayment.data.repository

import com.example.propaintersplastererspayment.data.local.dao.WorkEntryDao
import com.example.propaintersplastererspayment.data.local.entity.WorkEntryEntity
import com.example.propaintersplastererspayment.domain.repository.WorkEntryRepository
import kotlinx.coroutines.flow.Flow

class OfflineWorkEntryRepository(
    private val workEntryDao: WorkEntryDao
) : WorkEntryRepository {
    override fun observeEntriesForJob(jobId: Long): Flow<List<WorkEntryEntity>> =
        workEntryDao.observeEntriesForJob(jobId)

    override fun observeTotalHoursForJob(jobId: Long): Flow<Double> =
        workEntryDao.observeTotalHoursForJob(jobId)

    override suspend fun getEntry(entryId: Long): WorkEntryEntity? =
        workEntryDao.getEntryById(entryId)

    override suspend fun saveEntry(entry: WorkEntryEntity): Long {
        return if (entry.entryId == 0L) {
            workEntryDao.insertEntry(entry)
        } else {
            workEntryDao.updateEntry(entry)
            entry.entryId
        }
    }

    override suspend fun deleteEntry(entry: WorkEntryEntity) {
        workEntryDao.deleteEntry(entry)
    }
}


