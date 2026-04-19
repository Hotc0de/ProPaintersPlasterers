package com.example.propaintersplastererspayment.data.local.dao

import androidx.room.*
import com.example.propaintersplastererspayment.data.local.entity.PaintBrandEntity
import com.example.propaintersplastererspayment.data.local.entity.PaintItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaintDao {
    // Brands
    @Query("SELECT * FROM paint_brands WHERE isActive = 1 ORDER BY brandName ASC")
    fun getAllBrands(): Flow<List<PaintBrandEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBrand(brand: PaintBrandEntity): Long

    @Delete
    suspend fun deleteBrand(brand: PaintBrandEntity)

    // Paint Items
    @Query("SELECT * FROM paint_items WHERE brandId = :brandId AND isArchived = 0 ORDER BY paintName ASC")
    fun getPaintsForBrand(brandId: Long): Flow<List<PaintItemEntity>>

    @Query("SELECT * FROM paint_items WHERE paintId = :paintId LIMIT 1")
    fun getPaintById(paintId: Long): Flow<PaintItemEntity?>

    @Query("""
        SELECT paint_items.* FROM paint_items 
        INNER JOIN paint_brands ON paint_items.brandId = paint_brands.brandId
        WHERE isArchived = 0
        ORDER BY paint_brands.brandName ASC, paint_items.paintName ASC
    """)
    fun getAllPaintsWithBrand(): Flow<List<PaintItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaint(paint: PaintItemEntity): Long

    @Update
    suspend fun updatePaint(paint: PaintItemEntity)

    @Delete
    suspend fun deletePaint(paint: PaintItemEntity)
}
