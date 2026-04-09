package com.example.propaintersplastererspayment.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "work_entries",
    foreignKeys = [
        ForeignKey(
            entity = JobEntity::class,
            parentColumns = ["jobId"],
            childColumns = ["jobOwnerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["jobOwnerId"])]
)
data class WorkEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val entryId: Long = 0,
    val jobOwnerId: Long,
    val workDate: String,
    val workerName: String,
    val startTime: String,
    val finishTime: String,
    val hoursWorked: Double
)

