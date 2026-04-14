package com.example.propaintersplastererspayment.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.propaintersplastererspayment.data.local.entity.JobEntity
import com.example.propaintersplastererspayment.data.local.entity.JobStatus
import com.example.propaintersplastererspayment.data.local.model.JobWithInvoices
import com.example.propaintersplastererspayment.data.local.model.JobWithMaterials
import com.example.propaintersplastererspayment.data.local.model.JobWithWorkEntries
import kotlinx.coroutines.flow.Flow

@Dao
interface JobDao {
    @Query("SELECT * FROM jobs ORDER BY createdAt DESC")
    fun observeJobs(): Flow<List<JobEntity>>

    @Transaction
    @Query("SELECT * FROM jobs ORDER BY createdAt DESC")
    fun observeJobsWithInvoices(): Flow<List<JobWithInvoices>>

    @Query("SELECT * FROM jobs WHERE jobId = :jobId LIMIT 1")
    fun observeJob(jobId: Long): Flow<JobEntity?>

    @Transaction
    @Query("SELECT * FROM jobs WHERE jobId = :jobId LIMIT 1")
    fun observeJobWithWorkEntries(jobId: Long): Flow<JobWithWorkEntries?>

    @Transaction
    @Query("SELECT * FROM jobs WHERE jobId = :jobId LIMIT 1")
    fun observeJobWithMaterials(jobId: Long): Flow<JobWithMaterials?>

    @Transaction
    @Query("SELECT * FROM jobs WHERE jobId = :jobId LIMIT 1")
    fun observeJobWithInvoices(jobId: Long): Flow<JobWithInvoices?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: JobEntity): Long

    @Update
    suspend fun updateJob(job: JobEntity)

    @Delete
    suspend fun deleteJob(job: JobEntity)

    @Query("UPDATE jobs SET status = :status WHERE jobId = :jobId")
    suspend fun updateJobStatus(jobId: Long, status: JobStatus)
}

