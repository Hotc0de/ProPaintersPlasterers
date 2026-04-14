package com.example.propaintersplastererspayment.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "jobs",
    foreignKeys = [
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["clientId"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["clientId"])]
)
data class JobEntity(
    @PrimaryKey(autoGenerate = true)
    val jobId: Long = 0,
    val propertyAddress: String,
    val clientId: Long? = null,
    val clientNameSnapshot: String = "",
    val jobName: String = "",
    val notes: String = "",
    val status: JobStatus = JobStatus.WORKING,
    val createdAt: Long = System.currentTimeMillis(),
    val startDateOverride: Long? = null,
    val finishDateOverride: Long? = null
) {
    // Backward-compatible alias used by existing UI code.
    val clientName: String get() = clientNameSnapshot
}

