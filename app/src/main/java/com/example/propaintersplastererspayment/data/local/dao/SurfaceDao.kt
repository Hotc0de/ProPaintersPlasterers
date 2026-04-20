package com.example.propaintersplastererspayment.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.propaintersplastererspayment.data.local.entity.SurfaceEntity
import com.example.propaintersplastererspayment.data.local.model.SurfaceWithJobPaint
import kotlinx.coroutines.flow.Flow

@Dao
interface SurfaceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurface(surface: SurfaceEntity): Long

    @Update
    suspend fun updateSurface(surface: SurfaceEntity)

    @Delete
    suspend fun deleteSurface(surface: SurfaceEntity)

    @Query("SELECT * FROM surfaces WHERE surfaceId = :surfaceId LIMIT 1")
    suspend fun getSurfaceById(surfaceId: Long): SurfaceEntity?

    @Transaction
    @Query("SELECT * FROM surfaces WHERE surfaceId = :surfaceId LIMIT 1")
    fun observeSurfaceWithJobPaint(surfaceId: Long): Flow<SurfaceWithJobPaint?>

    @Transaction
    @Query("SELECT * FROM surfaces WHERE roomId = :roomId ORDER BY sortOrder ASC, surfaceId ASC")
    fun observeSurfacesForRoom(roomId: Long): Flow<List<SurfaceWithJobPaint>>

    @Query("UPDATE surfaces SET sortOrder = :sortOrder WHERE surfaceId = :surfaceId")
    suspend fun updateSurfaceSortOrder(surfaceId: Long, sortOrder: Int)

    @Query("UPDATE surfaces SET selectedJobPaintId = :jobPaintId WHERE surfaceId = :surfaceId")
    suspend fun updateSurfacePaint(surfaceId: Long, jobPaintId: Long?)
}
