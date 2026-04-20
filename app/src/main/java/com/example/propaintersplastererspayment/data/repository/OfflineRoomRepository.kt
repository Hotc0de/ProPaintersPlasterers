package com.example.propaintersplastererspayment.data.repository

import com.example.propaintersplastererspayment.data.local.dao.RoomDao
import com.example.propaintersplastererspayment.data.local.dao.SurfaceDao
import com.example.propaintersplastererspayment.data.local.entity.RoomEntity
import com.example.propaintersplastererspayment.data.local.entity.SurfaceEntity
import com.example.propaintersplastererspayment.data.local.model.RoomWithSurfaces
import com.example.propaintersplastererspayment.data.local.model.SurfaceWithJobPaint
import com.example.propaintersplastererspayment.domain.repository.RoomRepository
import com.example.propaintersplastererspayment.feature.job.util.RoomTemplateHelper
import kotlinx.coroutines.flow.Flow

class OfflineRoomRepository(
    private val roomDao: RoomDao,
    private val surfaceDao: SurfaceDao
) : RoomRepository {

    override fun observeRoomsForJob(jobId: Long): Flow<List<RoomWithSurfaces>> =
        roomDao.observeRoomsWithSurfacesForJob(jobId)

    override fun observeRoom(roomId: Long): Flow<RoomWithSurfaces?> =
        roomDao.observeRoomWithSurfaces(roomId)

    override suspend fun getRoomById(roomId: Long): RoomEntity? =
        roomDao.getRoomById(roomId)

    override suspend fun saveRoom(room: RoomEntity): Long =
        if (room.roomId == 0L) roomDao.insertRoom(room) else {
            roomDao.updateRoom(room)
            room.roomId
        }

    override suspend fun deleteRoom(room: RoomEntity) =
        roomDao.deleteRoom(room)

    override suspend fun updateRoomSortOrder(roomId: Long, sortOrder: Int) =
        roomDao.updateRoomSortOrder(roomId, sortOrder)

    override fun observeSurfacesForRoom(roomId: Long): Flow<List<SurfaceWithJobPaint>> =
        surfaceDao.observeSurfacesForRoom(roomId)

    override fun observeSurface(surfaceId: Long): Flow<SurfaceWithJobPaint?> =
        surfaceDao.observeSurfaceWithJobPaint(surfaceId)

    override suspend fun getSurfaceById(surfaceId: Long): SurfaceEntity? =
        surfaceDao.getSurfaceById(surfaceId)

    override suspend fun saveSurface(surface: SurfaceEntity): Long =
        if (surface.surfaceId == 0L) surfaceDao.insertSurface(surface) else {
            surfaceDao.updateSurface(surface)
            surface.surfaceId
        }

    override suspend fun deleteSurface(surface: SurfaceEntity) =
        surfaceDao.deleteSurface(surface)

    override suspend fun updateSurfaceSortOrder(surfaceId: Long, sortOrder: Int) =
        surfaceDao.updateSurfaceSortOrder(surfaceId, sortOrder)

    override suspend fun updateSurfacePaint(surfaceId: Long, jobPaintId: Long?) =
        surfaceDao.updateSurfacePaint(surfaceId, jobPaintId)

    override suspend fun duplicateSurface(surfaceId: Long): Long? {
        val existing = surfaceDao.getSurfaceById(surfaceId) ?: return null
        val newSurface = existing.copy(
            surfaceId = 0L,
            surfaceLabel = "${existing.surfaceLabel} (Copy)"
        )
        return surfaceDao.insertSurface(newSurface)
    }

    override suspend fun saveRoomWithTemplate(room: RoomEntity, useTemplate: Boolean): Long {
        val roomId = if (room.roomId == 0L) {
            roomDao.insertRoom(room)
        } else {
            roomDao.updateRoom(room)
            room.roomId
        }

        if (useTemplate && room.roomId == 0L) {
            val templateSurfaces = RoomTemplateHelper.createSurfacesFromTemplate(roomId, room.roomType)
            templateSurfaces.forEach { surfaceDao.insertSurface(it) }
        }

        return roomId
    }
}
