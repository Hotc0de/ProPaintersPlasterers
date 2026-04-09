package com.example.propaintersplastererspayment

import android.app.Application
import com.example.propaintersplastererspayment.data.AppContainer
import com.example.propaintersplastererspayment.data.AppDataContainer

class ProPaintersApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
        // First-run business setup is now handled by InitialBusinessSetupScreen.
        // The app checks for settings on launch and redirects if none are found.
    }
}
