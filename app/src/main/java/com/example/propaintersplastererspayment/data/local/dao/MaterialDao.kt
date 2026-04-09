package com.example.propaintersplastererspayment.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.propaintersplastererspayment.data.local.entity.MaterialItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MaterialDao {
    @Query("SELECT * FROM material_items WHERE jobOwnerId = :jobId ORDER BY materialId DESC")
    fun observeMaterialsForJob(jobId: Long): Flow<List<MaterialItemEntity>>

    @Query("SELECT * FROM material_items WHERE materialId = :materialId LIMIT 1")
    suspend fun getMaterialById(materialId: Long): MaterialItemEntity?

    @Query("SELECT COALESCE(SUM(price), 0) FROM material_items WHERE jobOwnerId = :jobId")
    fun observeTotalMaterialCostForJob(jobId: Long): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(item: MaterialItemEntity): Long

    @Update
    suspend fun updateMaterial(item: MaterialItemEntity)

    @Delete
    suspend fun deleteMaterial(item: MaterialItemEntity)
}


