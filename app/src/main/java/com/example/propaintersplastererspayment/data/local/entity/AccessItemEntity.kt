package com.example.propaintersplastererspayment.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "access_items",
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
data class AccessItemEntity(
    @PrimaryKey(autoGenerate = true)
    val accessId: Long = 0,
    val jobId: Long,
    val type: String, // e.g., "Alarm Code", "Lockbox", "Key Location", etc.
    val code: String,
    val instructions: String = ""
)
