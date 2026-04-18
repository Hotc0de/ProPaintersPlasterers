package com.example.propaintersplastererspayment.data.local.dao

import androidx.room.*
import com.example.propaintersplastererspayment.data.local.entity.AccessItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccessDao {
    @Query("SELECT * FROM access_items WHERE jobId = :jobId")
    fun observeAccessItems(jobId: Long): Flow<List<AccessItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccessItem(item: AccessItemEntity): Long

    @Update
    suspend fun updateAccessItem(item: AccessItemEntity)

    @Delete
    suspend fun deleteAccessItem(item: AccessItemEntity)
}
