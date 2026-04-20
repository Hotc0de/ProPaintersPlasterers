package com.example.propaintersplastererspayment.data.local.util

import androidx.room.TypeConverter
import com.example.propaintersplastererspayment.data.local.entity.JobStatus
import com.example.propaintersplastererspayment.data.local.entity.RoomType
import com.example.propaintersplastererspayment.data.local.entity.SurfaceType

class Converters {
    @TypeConverter
    fun fromJobStatus(status: JobStatus): String = status.name

    @TypeConverter
    fun toJobStatus(status: String): JobStatus = JobStatus.valueOf(status)

    @TypeConverter
    fun fromRoomType(type: RoomType): String = type.name

    @TypeConverter
    fun toRoomType(value: String): RoomType = try {
        RoomType.valueOf(value)
    } catch (e: Exception) {
        RoomType.BEDROOM
    }

    @TypeConverter
    fun fromSurfaceType(type: SurfaceType): String = type.name

    @TypeConverter
    fun toSurfaceType(value: String): SurfaceType = try {
        SurfaceType.valueOf(value)
    } catch (e: Exception) {
        SurfaceType.WALL
    }
}
