package com.example.propaintersplastererspayment.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "paint_items",
    foreignKeys = [
        ForeignKey(
            entity = PaintBrandEntity::class,
            parentColumns = ["brandId"],
            childColumns = ["brandId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["brandId"])]
)
data class PaintItemEntity(
    @PrimaryKey(autoGenerate = true)
    val paintId: Long = 0,
    val brandId: Long,
    val paintName: String,
    val paintCode: String = "",
    val hexCode: String = "",
    val finishType: String = "",
    val notes: String = "",
    val isArchived: Boolean = false
)
