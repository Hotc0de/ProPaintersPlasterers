package com.example.propaintersplastererspayment.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "property_access_items",
    foreignKeys = [
        ForeignKey(
            entity = PropertyAccessProfileEntity::class,
            parentColumns = ["profileId"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["profileId"])]
)
data class PropertyAccessItemEntity(
    @PrimaryKey(autoGenerate = true)
    val propertyAccessItemId: Long = 0,
    val profileId: Long,
    val type: String,
    val code: String,
    val instructions: String = ""
)
