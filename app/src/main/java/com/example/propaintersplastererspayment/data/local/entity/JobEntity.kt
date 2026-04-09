package com.example.propaintersplastererspayment.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "jobs")
data class JobEntity(
    @PrimaryKey(autoGenerate = true)
    val jobId: Long = 0,
    val propertyAddress: String,
    val clientName: String = "",
    val jobName: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

