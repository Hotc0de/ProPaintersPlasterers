package com.example.propaintersplastererspayment.app.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.propaintersplastererspayment.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Checks whether the user has completed the initial Business Settings setup.
 *
 * Used by [AppNavGraph] to decide the start destination on launch:
 *  - [SetupState.Loading]       → show a brief splash/loading screen
 *  - [SetupState.NeedsSetup]    → redirect to InitialBusinessSetupScreen
 *  - [SetupState.SetupComplete] → proceed to HomeScreen
 */
class AppStartupViewModel(
    settingsRepository: SettingsRepository
) : ViewModel() {

    sealed class SetupState {
        object Loading : SetupState()
        object NeedsSetup : SetupState()
        object SetupComplete : SetupState()
    }

    val setupState: StateFlow<SetupState> = settingsRepository.observeSettings()
        .map { settings ->
            if (settings == null || settings.businessName.isBlank()) {
                SetupState.NeedsSetup
            } else {
                SetupState.SetupComplete
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = SetupState.Loading
        )

    companion object {
        fun provideFactory(settingsRepository: SettingsRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    AppStartupViewModel(settingsRepository) as T
            }
    }
}

