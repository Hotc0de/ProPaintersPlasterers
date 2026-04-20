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
    @Query("""
        SELECT 
            s.*,
            uc_p.paintId as undercoatPaintId, 
            uc_b.brandName as undercoatBrandName, 
            uc_p.paintName as undercoatPaintName, 
            uc_p.hexCode as undercoatHexCode,
            mc_p.paintId as maincoatPaintId, 
            mc_b.brandName as maincoatBrandName, 
            mc_p.paintName as maincoatPaintName, 
            mc_p.hexCode as maincoatHexCode
        FROM surfaces s
        LEFT JOIN job_paints uc_jp ON s.undercoatJobPaintId = uc_jp.jobPaintId
        LEFT JOIN paint_items uc_p ON uc_jp.paintId = uc_p.paintId
        LEFT JOIN paint_brands uc_b ON uc_p.brandId = uc_b.brandId
        LEFT JOIN job_paints mc_jp ON s.maincoatJobPaintId = mc_jp.jobPaintId
        LEFT JOIN paint_items mc_p ON mc_jp.paintId = mc_p.paintId
        LEFT JOIN paint_brands mc_b ON mc_p.brandId = mc_b.brandId
        WHERE s.surfaceId = :surfaceId LIMIT 1
    """)
    fun observeSurfaceWithJobPaint(surfaceId: Long): Flow<SurfaceWithJobPaint?>

    @Transaction
    @Query("""
        SELECT 
            s.*,
            uc_p.paintId as undercoatPaintId, 
            uc_b.brandName as undercoatBrandName, 
            uc_p.paintName as undercoatPaintName, 
            uc_p.hexCode as undercoatHexCode,
            mc_p.paintId as maincoatPaintId, 
            mc_b.brandName as maincoatBrandName, 
            mc_p.paintName as maincoatPaintName, 
            mc_p.hexCode as maincoatHexCode
        FROM surfaces s
        LEFT JOIN job_paints uc_jp ON s.undercoatJobPaintId = uc_jp.jobPaintId
        LEFT JOIN paint_items uc_p ON uc_jp.paintId = uc_p.paintId
        LEFT JOIN paint_brands uc_b ON uc_p.brandId = uc_b.brandId
        LEFT JOIN job_paints mc_jp ON s.maincoatJobPaintId = mc_jp.jobPaintId
        LEFT JOIN paint_items mc_p ON mc_jp.paintId = mc_p.paintId
        LEFT JOIN paint_brands mc_b ON mc_p.brandId = mc_b.brandId
        WHERE s.roomId = :roomId ORDER BY s.sortOrder ASC, s.surfaceId ASC
    """)
    fun observeSurfacesForRoom(roomId: Long): Flow<List<SurfaceWithJobPaint>>

    @Query("UPDATE surfaces SET sortOrder = :sortOrder WHERE surfaceId = :surfaceId")
    suspend fun updateSurfaceSortOrder(surfaceId: Long, sortOrder: Int)

    @Query("UPDATE surfaces SET maincoatJobPaintId = :jobPaintId WHERE surfaceId = :surfaceId")
    suspend fun updateSurfacePaint(surfaceId: Long, jobPaintId: Long?)
}
