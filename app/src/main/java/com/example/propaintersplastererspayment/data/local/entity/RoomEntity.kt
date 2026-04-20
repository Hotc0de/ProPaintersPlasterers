package com.example.propaintersplastererspayment.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rooms",
    foreignKeys = [
        ForeignKey(
            entity = JobEntity::class,
            parentColumns = ["jobId"],
            childColumns = ["jobId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["jobId"])]
)
data class RoomEntity(
    @PrimaryKey(autoGenerate = true)
    val roomId: Long = 0,
    val jobId: Long,
    val roomType: RoomType = RoomType.BEDROOM,
    val roomName: String = "",
    val level: String? = null,
    val notes: String? = null,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
