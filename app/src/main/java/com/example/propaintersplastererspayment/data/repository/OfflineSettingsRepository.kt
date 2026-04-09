package com.example.propaintersplastererspayment.data.repository

import com.example.propaintersplastererspayment.data.local.dao.AppSettingsDao
import com.example.propaintersplastererspayment.data.local.entity.AppSettingsEntity
import com.example.propaintersplastererspayment.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class OfflineSettingsRepository(
    private val appSettingsDao: AppSettingsDao
) : SettingsRepository {
    override fun observeSettings(): Flow<AppSettingsEntity?> = appSettingsDao.observeSettings()

    override suspend fun saveSettings(settings: AppSettingsEntity) {
        appSettingsDao.saveSettings(settings)
    }
}

