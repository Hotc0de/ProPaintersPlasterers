package com.example.propaintersplastererspayment.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "paint_brands")
data class PaintBrandEntity(
    @PrimaryKey(autoGenerate = true)
    val brandId: Long = 0,
    val brandName: String,
    val notes: String = "",
    val isActive: Boolean = true
)
