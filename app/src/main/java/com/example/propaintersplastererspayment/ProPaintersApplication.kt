package com.example.propaintersplastererspayment

import android.app.Application
import com.example.propaintersplastererspayment.data.AppContainer
import com.example.propaintersplastererspayment.data.AppDataContainer
import com.example.propaintersplastererspayment.data.local.entity.AppSettingsEntity
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ProPaintersApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)

        // Initialize default settings on first app launch
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch {
            container.settingsRepository.observeSettings().collect { settings ->
                if (settings == null) {
                    container.settingsRepository.saveSettings(
                        AppSettingsEntity(
                            settingsId = 1,
                            businessName = "Your Business Name",
                            address = "Your Address",
                            phoneNumber = "",
                            email = "",
                            gstNumber = "",
                            bankAccountNumber = "",
                            invoiceNumberPrefix = "INV-",
                            defaultLabourRate = 65.0,
                            defaultGstRate = 0.15,
                            gstEnabledByDefault = true
                        )
                    )
                }
            }
        }
    }
}

