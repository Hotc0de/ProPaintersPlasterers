package com.example.propaintersplastererspayment.domain.repository

import com.example.propaintersplastererspayment.data.local.entity.JobEntity
import com.example.propaintersplastererspayment.data.local.entity.JobStatus
import com.example.propaintersplastererspayment.data.local.model.JobWithInvoices
import kotlinx.coroutines.flow.Flow

interface JobRepository {
    fun observeJobs(): Flow<List<JobEntity>>
    fun observeJobsWithInvoices(): Flow<List<JobWithInvoices>>
    fun observeJob(jobId: Long): Flow<JobEntity?>
    suspend fun saveJob(job: JobEntity): Long
    suspend fun deleteJob(job: JobEntity)
    suspend fun updateJobStatus(jobId: Long, status: JobStatus)
    suspend fun updateJobDates(jobId: Long, startDate: Long?, finishDate: Long?)
}

