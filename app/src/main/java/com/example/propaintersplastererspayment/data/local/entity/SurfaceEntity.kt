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
            childColumns = ["selectedJobPaintId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["roomId"]),
        Index(value = ["selectedJobPaintId"])
    ]
)
data class SurfaceEntity(
    @PrimaryKey(autoGenerate = true)
    val surfaceId: Long = 0,
    val roomId: Long,
    val surfaceType: SurfaceType = SurfaceType.WALL,
    val surfaceLabel: String = "",
    val selectedJobPaintId: Long? = null,
    val finishTypeOverride: String? = null,
    val coatCount: Int = 2,
    val isFeatureSurface: Boolean = false,
    val notes: String? = null,
    val sortOrder: Int = 0,
    
    // Future expansion fields
    val surfaceCount: Int = 1,
    val areaSize: Double? = null,
    val areaUnit: String? = null // e.g., "m2", "sqft"
)
