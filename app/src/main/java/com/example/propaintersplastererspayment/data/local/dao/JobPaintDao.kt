package com.example.propaintersplastererspayment.data.local.dao

import androidx.room.*
import com.example.propaintersplastererspayment.data.local.entity.JobPaintEntity
import com.example.propaintersplastererspayment.data.local.entity.PaintItemEntity
import com.example.propaintersplastererspayment.data.local.entity.PaintBrandEntity
import kotlinx.coroutines.flow.Flow

data class JobPaintWithDetails(
    @Embedded val jobPaint: JobPaintEntity,
    @Relation(
        parentColumn = "paintId",
        entityColumn = "paintId"
    )
    val paint: PaintItemEntity,
    @Relation(
        parentColumn = "paintId",
        entityColumn = "brandId",
        associateBy = Junction(
            value = PaintItemEntity::class,
            parentColumn = "paintId",
            entityColumn = "brandId"
        )
    )
    val brand: PaintBrandEntity? = null // This might need a simpler POJO if the Relation/Junction is complex
)

// Simpler POJO for Job Paint display
data class JobPaintDisplay(
    val jobPaintId: Long,
    val paintId: Long,
    val brandName: String,
    val paintName: String,
    val paintCode: String,
    val finishType: String,
    val hexCode: String,
    val paintScope: String,
    val notes: String
)

@Dao
interface JobPaintDao {
    @Query("""
        SELECT 
            jp.jobPaintId, 
            p.paintId, 
            b.brandName, 
            p.paintName, 
            p.paintCode, 
            p.finishType,
            p.hexCode, 
            p.paintScope,
            jp.notes
        FROM job_paints jp
        INNER JOIN paint_items p ON jp.paintId = p.paintId
        INNER JOIN paint_brands b ON p.brandId = b.brandId
        WHERE jp.jobId = :jobId
    """)
    fun getPaintsForJob(jobId: Long): Flow<List<JobPaintDisplay>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addPaintToJob(jobPaint: JobPaintEntity): Long

    @Delete
    suspend fun removePaintFromJob(jobPaint: JobPaintEntity)
    
    @Query("DELETE FROM job_paints WHERE jobPaintId = :jobPaintId")
    suspend fun deleteJobPaintById(jobPaintId: Long)
}
