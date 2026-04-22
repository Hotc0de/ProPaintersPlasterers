package com.example.propaintersplastererspayment.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    // Keep a single row for app-wide settings.
    @PrimaryKey
    val settingsId: Int = 1,
    val businessName: String = "",
    val address: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val gstNumber: String = "",
    val bankAccountNumber: String = "",
    val bankName: String = "",
    val defaultLabourRate: Double = 0.0,
    val defaultGstRate: Double = 0.15,
    val gstEnabledByDefault: Boolean = true
)

