package com.example.propaintersplastererspayment.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.propaintersplastererspayment.data.local.entity.AccessItemEntity
import com.example.propaintersplastererspayment.data.local.entity.JobEntity
import com.example.propaintersplastererspayment.data.local.entity.JobStatus
import com.example.propaintersplastererspayment.data.local.entity.PropertyAccessItemEntity
import com.example.propaintersplastererspayment.data.local.model.JobWithInvoices
import com.example.propaintersplastererspayment.data.local.model.JobWithMaterials
import com.example.propaintersplastererspayment.data.local.model.JobWithWorkEntries
import kotlinx.coroutines.flow.Flow

@Dao
interface JobDao {
    @Query("SELECT * FROM jobs ORDER BY createdAt DESC")
    fun observeJobs(): Flow<List<JobEntity>>

    @Transaction
    @Query("SELECT * FROM jobs WHERE isQuickInvoice = 0 ORDER BY createdAt DESC")
    fun observeJobsWithInvoices(): Flow<List<JobWithInvoices>>

    @Transaction
    @Query("SELECT * FROM jobs WHERE isQuickInvoice = 1 ORDER BY createdAt DESC")
    fun observeQuickInvoicesWithInvoices(): Flow<List<JobWithInvoices>>

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

    @Query("SELECT profileId FROM property_access_profiles WHERE addressKey = :addressKey LIMIT 1")
    suspend fun getPropertyAccessProfileId(addressKey: String): Long?

    @Query("SELECT * FROM property_access_items WHERE profileId = :profileId ORDER BY propertyAccessItemId")
    suspend fun getPropertyAccessItems(profileId: Long): List<PropertyAccessItemEntity>

    @Insert
    suspend fun insertAccessItems(items: List<AccessItemEntity>)

    @Transaction
    suspend fun insertJobWithReusableAccess(job: JobEntity, addressKey: String): Long {
        val jobId = insertJob(job)
        val profileId = getPropertyAccessProfileId(addressKey) ?: return jobId
        val reusableItems = getPropertyAccessItems(profileId)
        if (reusableItems.isNotEmpty()) {
            insertAccessItems(
                reusableItems.map { item ->
                    AccessItemEntity(
                        jobId = jobId,
                        type = item.type,
                        code = item.code,
                        instructions = item.instructions
                    )
                }
            )
        }
        return jobId
    }

    @Update
    suspend fun updateJob(job: JobEntity)

    @Delete
    suspend fun deleteJob(job: JobEntity)

    @Query("DELETE FROM jobs WHERE jobId = :jobId")
    suspend fun deleteJobById(jobId: Long)

    @Query("UPDATE jobs SET status = :status WHERE jobId = :jobId")
    suspend fun updateJobStatus(jobId: Long, status: JobStatus)

    @Query("UPDATE jobs SET startDateOverride = :startDate, finishDateOverride = :finishDate WHERE jobId = :jobId")
    suspend fun updateJobDates(jobId: Long, startDate: Long?, finishDate: Long?)

    @Query("UPDATE jobs SET accessInfo = :accessInfo WHERE jobId = :jobId")
    suspend fun updateJobAccessInfo(jobId: Long, accessInfo: String)

    @Query("UPDATE jobs SET notes = :notes WHERE jobId = :jobId")
    suspend fun updateJobNotes(jobId: Long, notes: String)

    @Query("SELECT DISTINCT propertyAddress FROM jobs WHERE clientId = :clientId AND propertyAddress != ''")
    suspend fun getPropertyAddressesForClient(clientId: Long): List<String>
}
