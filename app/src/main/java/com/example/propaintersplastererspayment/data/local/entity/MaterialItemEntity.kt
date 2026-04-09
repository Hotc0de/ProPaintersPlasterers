package com.example.propaintersplastererspayment.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "material_items",
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
data class MaterialItemEntity(
    @PrimaryKey(autoGenerate = true)
    val materialId: Long = 0,
    val jobOwnerId: Long,
    val materialName: String,
    val price: Double
)

