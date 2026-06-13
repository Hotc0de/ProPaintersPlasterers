package com.example.propaintersplastererspayment.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "property_access_profiles",
    indices = [Index(value = ["addressKey"], unique = true)]
)
data class PropertyAccessProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val profileId: Long = 0,
    val addressKey: String,
    val displayAddress: String,
    val updatedAt: Long = System.currentTimeMillis()
)
