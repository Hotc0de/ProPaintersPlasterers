package com.example.propaintersplastererspayment.data.repository

import com.example.propaintersplastererspayment.data.local.dao.JobDao
import com.example.propaintersplastererspayment.data.local.entity.JobEntity
import com.example.propaintersplastererspayment.data.local.entity.JobStatus
import com.example.propaintersplastererspayment.data.local.model.JobWithInvoices
import com.example.propaintersplastererspayment.domain.repository.JobRepository
import kotlinx.coroutines.flow.Flow

class OfflineJobRepository(
    private val jobDao: JobDao
) : JobRepository {
    override fun observeJobs(): Flow<List<JobEntity>> = jobDao.observeJobs()

    override fun observeJobsWithInvoices(): Flow<List<JobWithInvoices>> = jobDao.observeJobsWithInvoices()

    override fun observeQuickInvoicesWithInvoices(): Flow<List<JobWithInvoices>> = jobDao.observeQuickInvoicesWithInvoices()

    override fun observeJob(jobId: Long): Flow<JobEntity?> = jobDao.observeJob(jobId)

    override suspend fun saveJob(job: JobEntity): Long {
        return if (job.jobId == 0L) {
            jobDao.insertJob(job)
        } else {
            jobDao.updateJob(job)
            job.jobId
        }
    }

    override suspend fun deleteJob(job: JobEntity) {
        jobDao.deleteJob(job)
    }

    override suspend fun deleteJobById(jobId: Long) {
        jobDao.deleteJobById(jobId)
    }

    override suspend fun updateJobStatus(jobId: Long, status: JobStatus) {
        jobDao.updateJobStatus(jobId, status)
    }

    override suspend fun updateJobDates(jobId: Long, startDate: Long?, finishDate: Long?) {
        jobDao.updateJobDates(jobId, startDate, finishDate)
    }
}

