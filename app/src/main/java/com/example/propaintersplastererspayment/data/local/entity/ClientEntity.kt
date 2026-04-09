package com.example.propaintersplastererspayment.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "clients",
    indices = [Index(value = ["name"], unique = true)]
)
data class ClientEntity(
    @PrimaryKey(autoGenerate = true)
    val clientId: Long = 0,
    /** "PRIVATE" or "BUSINESS" */
    val clientType: String = "PRIVATE",
    val name: String,
    val address: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val notes: String = ""
)

