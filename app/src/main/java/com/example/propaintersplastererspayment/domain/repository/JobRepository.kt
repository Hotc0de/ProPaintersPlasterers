package com.example.propaintersplastererspayment.domain.repository

import com.example.propaintersplastererspayment.data.local.entity.JobEntity
import kotlinx.coroutines.flow.Flow

interface JobRepository {
    fun observeJobs(): Flow<List<JobEntity>>
    fun observeJob(jobId: Long): Flow<JobEntity?>
    suspend fun saveJob(job: JobEntity): Long
    suspend fun deleteJob(job: JobEntity)
}

