package com.example.propaintersplastererspayment.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.propaintersplastererspayment.data.local.entity.RoomEntity
import com.example.propaintersplastererspayment.data.local.entity.SurfaceEntity

data class RoomWithSurfaces(
    @Embedded val room: RoomEntity,
    @Relation(
        entity = SurfaceEntity::class,
        parentColumn = "roomId",
        entityColumn = "roomId"
    )
    val surfaces: List<SurfaceEntity>
)
