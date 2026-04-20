package com.example.propaintersplastererspayment.domain.repository

import com.example.propaintersplastererspayment.data.local.entity.RoomEntity
import com.example.propaintersplastererspayment.data.local.entity.SurfaceEntity
import com.example.propaintersplastererspayment.data.local.model.RoomWithSurfaces
import com.example.propaintersplastererspayment.data.local.model.SurfaceWithJobPaint
import kotlinx.coroutines.flow.Flow

interface RoomRepository {
    // Room operations
    fun observeRoomsForJob(jobId: Long): Flow<List<RoomWithSurfaces>>
    fun observeRoom(roomId: Long): Flow<RoomWithSurfaces?>
    suspend fun getRoomById(roomId: Long): RoomEntity?
    suspend fun saveRoom(room: RoomEntity): Long
    suspend fun deleteRoom(room: RoomEntity)
    suspend fun updateRoomSortOrder(roomId: Long, sortOrder: Int)

    // Surface operations
    fun observeSurfacesForRoom(roomId: Long): Flow<List<SurfaceWithJobPaint>>
    fun observeSurface(surfaceId: Long): Flow<SurfaceWithJobPaint?>
    suspend fun getSurfaceById(surfaceId: Long): SurfaceEntity?
    suspend fun saveSurface(surface: SurfaceEntity): Long
    suspend fun deleteSurface(surface: SurfaceEntity)
    suspend fun updateSurfaceSortOrder(surfaceId: Long, sortOrder: Int)
    suspend fun updateSurfacePaint(surfaceId: Long, jobPaintId: Long?)
    
    // Composite operations
    suspend fun duplicateSurface(surfaceId: Long): Long?
}
