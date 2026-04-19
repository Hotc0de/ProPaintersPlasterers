package com.example.propaintersplastererspayment.data.repository

import com.example.propaintersplastererspayment.data.local.dao.JobPaintDao
import com.example.propaintersplastererspayment.data.local.dao.JobPaintDisplay
import com.example.propaintersplastererspayment.data.local.dao.PaintDao
import com.example.propaintersplastererspayment.data.local.entity.JobPaintEntity
import com.example.propaintersplastererspayment.data.local.entity.PaintBrandEntity
import com.example.propaintersplastererspayment.data.local.entity.PaintItemEntity
import com.example.propaintersplastererspayment.domain.repository.PaintRepository
import kotlinx.coroutines.flow.Flow

class OfflinePaintRepository(
    private val paintDao: PaintDao,
    private val jobPaintDao: JobPaintDao
) : PaintRepository {
    override fun getAllBrandsStream(): Flow<List<PaintBrandEntity>> = paintDao.getAllBrands()
    
    override suspend fun insertBrand(brand: PaintBrandEntity): Long = paintDao.insertBrand(brand)
    
    override suspend fun deleteBrand(brand: PaintBrandEntity) = paintDao.deleteBrand(brand)

    override fun getPaintsForBrandStream(brandId: Long): Flow<List<PaintItemEntity>> = 
        paintDao.getPaintsForBrand(brandId)

    override fun getPaintByIdStream(paintId: Long): Flow<PaintItemEntity?> =
        paintDao.getPaintById(paintId)

    override fun getAllPaintsWithBrandStream(): Flow<List<PaintItemEntity>> = 
        paintDao.getAllPaintsWithBrand()

    override suspend fun insertPaint(paint: PaintItemEntity): Long = paintDao.insertPaint(paint)

    override suspend fun updatePaint(paint: PaintItemEntity) = paintDao.updatePaint(paint)

    override suspend fun deletePaint(paint: PaintItemEntity) = paintDao.deletePaint(paint)

    override fun getPaintsForJobStream(jobId: Long): Flow<List<JobPaintDisplay>> = 
        jobPaintDao.getPaintsForJob(jobId)

    override suspend fun addPaintToJob(jobPaint: JobPaintEntity) {
        jobPaintDao.addPaintToJob(jobPaint)
    }

    override suspend fun removePaintFromJob(jobPaintId: Long) {
        jobPaintDao.deleteJobPaintById(jobPaintId)
    }
}
