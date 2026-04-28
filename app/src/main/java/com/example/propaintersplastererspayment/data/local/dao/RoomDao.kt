package com.example.propaintersplastererspayment.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.propaintersplastererspayment.data.local.entity.RoomEntity
import com.example.propaintersplastererspayment.data.local.model.RoomWithSurfaces
import com.example.propaintersplastererspayment.data.local.model.JobWithRooms
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: RoomEntity): Long

    @Update
    suspend fun updateRoom(room: RoomEntity)

    @Delete
    suspend fun deleteRoom(room: RoomEntity)

    @Query("SELECT * FROM rooms WHERE roomId = :roomId LIMIT 1")
    suspend fun getRoomById(roomId: Long): RoomEntity?

    @Transaction
    @Query("""
        SELECT 
            r.*,
            (SELECT mc_p.hexCode 
             FROM surfaces s
             LEFT JOIN job_paints mc_jp ON s.maincoatJobPaintId = mc_jp.jobPaintId
             LEFT JOIN paint_items mc_p ON mc_jp.paintId = mc_p.paintId
             WHERE s.roomId = r.roomId AND s.maincoatJobPaintId IS NOT NULL
             LIMIT 1) as maincoatHexCode
        FROM rooms r 
        WHERE r.roomId = :roomId 
        LIMIT 1
    """)
    fun observeRoomWithSurfaces(roomId: Long): Flow<RoomWithSurfaces?>

    @Transaction
    @Query("""
        SELECT 
            r.*,
            (SELECT mc_p.hexCode 
             FROM surfaces s
             LEFT JOIN job_paints mc_jp ON s.maincoatJobPaintId = mc_jp.jobPaintId
             LEFT JOIN paint_items mc_p ON mc_jp.paintId = mc_p.paintId
             WHERE s.roomId = r.roomId AND s.maincoatJobPaintId IS NOT NULL
             LIMIT 1) as maincoatHexCode
        FROM rooms r 
        WHERE r.jobId = :jobId 
        ORDER BY r.sortOrder ASC, r.createdAt ASC
    """)
    fun observeRoomsWithSurfacesForJob(jobId: Long): Flow<List<RoomWithSurfaces>>

    @Transaction
    @Query("SELECT * FROM jobs WHERE jobId = :jobId LIMIT 1")
    fun observeJobWithRooms(jobId: Long): Flow<JobWithRooms?>

    @Query("UPDATE rooms SET sortOrder = :sortOrder WHERE roomId = :roomId")
    suspend fun updateRoomSortOrder(roomId: Long, sortOrder: Int)
}
