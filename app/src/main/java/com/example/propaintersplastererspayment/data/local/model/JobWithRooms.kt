package com.example.propaintersplastererspayment.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.propaintersplastererspayment.data.local.entity.JobEntity
import com.example.propaintersplastererspayment.data.local.entity.RoomEntity

data class JobWithRooms(
    @Embedded val job: JobEntity,
    @Relation(
        entity = RoomEntity::class,
        parentColumn = "jobId",
        entityColumn = "jobId"
    )
    val rooms: List<RoomWithSurfaces>
)
