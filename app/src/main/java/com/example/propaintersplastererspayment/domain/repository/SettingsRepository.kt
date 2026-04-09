package com.example.propaintersplastererspayment.domain.repository

import com.example.propaintersplastererspayment.data.local.entity.AppSettingsEntity
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeSettings(): Flow<AppSettingsEntity?>
    suspend fun saveSettings(settings: AppSettingsEntity)
}

