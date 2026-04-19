package com.example.propaintersplastererspayment.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "job_paints",
    foreignKeys = [
        ForeignKey(
            entity = JobEntity::class,
            parentColumns = ["jobId"],
            childColumns = ["jobId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PaintItemEntity::class,
            parentColumns = ["paintId"],
            childColumns = ["paintId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["jobId"]),
        Index(value = ["paintId"]),
        // Prevent duplicate paint entry for the same job
        Index(value = ["jobId", "paintId"], unique = true)
    ]
)
data class JobPaintEntity(
    @PrimaryKey(autoGenerate = true)
    val jobPaintId: Long = 0,
    val jobId: Long,
    val paintId: Long,
    val notes: String = ""
)
