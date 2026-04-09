package com.example.propaintersplastererspayment.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.propaintersplastererspayment.data.local.entity.WorkEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkEntryDao {
    @Query("SELECT * FROM work_entries WHERE jobOwnerId = :jobId ORDER BY workDate DESC, startTime ASC")
    fun observeEntriesForJob(jobId: Long): Flow<List<WorkEntryEntity>>

    @Query("SELECT * FROM work_entries WHERE entryId = :entryId LIMIT 1")
    suspend fun getEntryById(entryId: Long): WorkEntryEntity?

    @Query("SELECT COALESCE(SUM(hoursWorked), 0) FROM work_entries WHERE jobOwnerId = :jobId")
    fun observeTotalHoursForJob(jobId: Long): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: WorkEntryEntity): Long

    @Update
    suspend fun updateEntry(entry: WorkEntryEntity)

    @Delete
    suspend fun deleteEntry(entry: WorkEntryEntity)
}


