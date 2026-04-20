package com.example.propaintersplastererspayment.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "surfaces",
    foreignKeys = [
        ForeignKey(
            entity = RoomEntity::class,
            parentColumns = ["roomId"],
            childColumns = ["roomId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = JobPaintEntity::class,
            parentColumns = ["jobPaintId"],
            childColumns = ["undercoatJobPaintId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = JobPaintEntity::class,
            parentColumns = ["jobPaintId"],
            childColumns = ["maincoatJobPaintId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["roomId"]),
        Index(value = ["undercoatJobPaintId"]),
        Index(value = ["maincoatJobPaintId"])
    ]
)
data class SurfaceEntity(
    @PrimaryKey(autoGenerate = true)
    val surfaceId: Long = 0,
    val roomId: Long,
    val surfaceType: SurfaceType = SurfaceType.WALL,
    val customName: String = "",
    val displayName: String = "",
    val notes: String? = null,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),

    // Paint-related fields
    val undercoatJobPaintId: Long? = null,
    val maincoatJobPaintId: Long? = null,
    val maincoatCoatCount: Int = 2,
    
    val finishTypeOverride: String? = null,
    val isFeatureSurface: Boolean = false,

    // Future expansion fields
    val surfaceCount: Int = 1,
    val areaSize: Double? = null,
    val areaUnit: String? = null // e.g., "m2", "sqft"
)
