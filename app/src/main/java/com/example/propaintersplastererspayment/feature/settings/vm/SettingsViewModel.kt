package com.example.propaintersplastererspayment.feature.settings.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.propaintersplastererspayment.data.local.entity.AppSettingsEntity
import com.example.propaintersplastererspayment.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// Form state
// ─────────────────────────────────────────────────────────────────────────────

data class SettingsFormState(
    val businessName: String = "",
    val address: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val gstNumber: String = "",
    val bankAccountNumber: String = "",
    val invoiceNumberPrefix: String = "INV",
    val defaultLabourRateText: String = "",
    val defaultGstPercentText: String = "15",
    val gstEnabledByDefault: Boolean = true,
    val errorMessage: String? = null
) {
    val parsedLabourRate: Double?
        get() = defaultLabourRateText.trim().replace(",", "").toDoubleOrNull()

    val parsedGstPercent: Double?
        get() = defaultGstPercentText.trim().replace(",", "").toDoubleOrNull()

    val isValid: Boolean
        get() = businessName.isNotBlank() &&
                address.isNotBlank() &&
                phoneNumber.isNotBlank() &&
                email.isNotBlank() &&
                bankAccountNumber.isNotBlank() &&
                parsedLabourRate != null &&
                parsedLabourRate!! > 0 &&
                parsedGstPercent != null &&
                parsedGstPercent!! >= 0
}

// ─────────────────────────────────────────────────────────────────────────────
// UI state
// ─────────────────────────────────────────────────────────────────────────────

data class SettingsUiState(
    val settings: AppSettingsEntity? = null,
    val formState: SettingsFormState = SettingsFormState(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val userMessage: String? = null,
    val lastSavedAt: String? = null
)

// ─────────────────────────────────────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────────────────────────────────────

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val formState = MutableStateFlow(SettingsFormState())
    private val isSaving = MutableStateFlow(false)
    private val userMessage = MutableStateFlow<String?>(null)
    private val lastSavedAt = MutableStateFlow<String?>(null)

    /** Fires once after a successful save — used by InitialSetupScreen for navigation. */
    private val _settingsSaved = MutableSharedFlow<Unit>()
    val settingsSaved: SharedFlow<Unit> = _settingsSaved.asSharedFlow()

    // Tracks whether the settings flow has emitted at least once (even if null).
    // This prevents showing a permanent loading spinner on first-run setup.
    private var settingsLoaded = false

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.observeSettings().onEach { settingsLoaded = true },
        formState,
        isSaving,
        userMessage,
        lastSavedAt
    ) { settings, form, saving, message, savedAt ->
        SettingsUiState(
            settings = settings,
            formState = form,
            isLoading = !settingsLoaded,
            isSaving = saving,
            userMessage = message,
            lastSavedAt = savedAt
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    init {
        viewModelScope.launch {
            val firstSettings = settingsRepository.observeSettings().first()
            if (firstSettings != null && formState.value.businessName.isEmpty()) {
                formState.value = SettingsFormState(
                    businessName = firstSettings.businessName,
                    address = firstSettings.address,
                    phoneNumber = firstSettings.phoneNumber,
                    email = firstSettings.email,
                    gstNumber = firstSettings.gstNumber,
                    bankAccountNumber = firstSettings.bankAccountNumber,
                    invoiceNumberPrefix = firstSettings.invoiceNumberPrefix.trimEnd('-').ifBlank { "INV" },
                    defaultLabourRateText = if (firstSettings.defaultLabourRate > 0) {
                        firstSettings.defaultLabourRate.toString()
                    } else {
                        ""
                    },
                    defaultGstPercentText = (firstSettings.defaultGstRate * 100).toInt().toString(),
                    gstEnabledByDefault = firstSettings.gstEnabledByDefault
                )
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Form field updates
    // ─────────────────────────────────────────────────────────────────────

    fun onBusinessNameChange(value: String) {
        formState.update { it.copy(businessName = value, errorMessage = null) }
    }

    fun onAddressChange(value: String) {
        formState.update { it.copy(address = value, errorMessage = null) }
    }

    fun onPhoneNumberChange(value: String) {
        formState.update { it.copy(phoneNumber = value, errorMessage = null) }
    }

    fun onEmailChange(value: String) {
        formState.update { it.copy(email = value, errorMessage = null) }
    }

    fun onGstNumberChange(value: String) {
        formState.update { it.copy(gstNumber = value, errorMessage = null) }
    }

    fun onBankAccountNumberChange(value: String) {
        formState.update { it.copy(bankAccountNumber = value, errorMessage = null) }
    }

    fun onInvoicePrefixChange(value: String) {
        formState.update { it.copy(invoiceNumberPrefix = value, errorMessage = null) }
    }

    fun onDefaultLabourRateChange(value: String) {
        formState.update { it.copy(defaultLabourRateText = value, errorMessage = null) }
    }

    fun onDefaultGstPercentChange(value: String) {
        formState.update { it.copy(defaultGstPercentText = value, errorMessage = null) }
    }

    fun onGstEnabledChange(value: Boolean) {
        formState.update { it.copy(gstEnabledByDefault = value) }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Save action
    // ─────────────────────────────────────────────────────────────────────

    fun saveSettings() {
        val form = formState.value
        val validationError = validateForm(form)
        if (validationError != null) {
            formState.update { it.copy(errorMessage = validationError) }
            return
        }

        isSaving.value = true

        viewModelScope.launch {
            try {
                val labourRate = form.parsedLabourRate ?: 0.0
                val gstRate = (form.parsedGstPercent ?: 0.0) / 100.0

                settingsRepository.saveSettings(
                    AppSettingsEntity(
                        settingsId = 1,
                        businessName = form.businessName.trim(),
                        address = form.address.trim(),
                        phoneNumber = form.phoneNumber.trim(),
                        email = form.email.trim(),
                        gstNumber = form.gstNumber.trim(),
                        bankAccountNumber = form.bankAccountNumber.trim(),
                        invoiceNumberPrefix = form.invoiceNumberPrefix.trim().trimEnd('-').ifBlank { "INV" },
                        defaultLabourRate = labourRate,
                        defaultGstRate = gstRate,
                        gstEnabledByDefault = form.gstEnabledByDefault
                    )
                )

                userMessage.value = "Settings saved successfully."
                lastSavedAt.value = getCurrentTimeString()
                formState.update { it.copy(errorMessage = null) }
                _settingsSaved.emit(Unit)
            } catch (e: Exception) {
                userMessage.value = "Error saving settings: ${e.message}"
            } finally {
                isSaving.value = false
            }
        }
    }

    fun clearUserMessage() {
        userMessage.value = null
    }

    // ─────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────

    private fun validateForm(form: SettingsFormState): String? = when {
        form.businessName.isBlank() -> "Business name is required."
        form.address.isBlank() -> "Address is required."
        form.phoneNumber.isBlank() -> "Phone number is required."
        form.email.isBlank() -> "Email is required."
        !isValidEmail(form.email) -> "Please enter a valid email address."
        form.bankAccountNumber.isBlank() -> "Bank account number is required."
        form.invoiceNumberPrefix.isBlank() -> "Invoice prefix is required."
        form.defaultLabourRateText.isBlank() -> "Default labour rate is required."
        form.parsedLabourRate == null -> "Labour rate must be a valid number."
        form.parsedLabourRate!! <= 0 -> "Labour rate must be greater than 0."
        form.defaultGstPercentText.isBlank() -> "Default GST percent is required."
        form.parsedGstPercent == null -> "GST percent must be a valid number."
        form.parsedGstPercent!! < 0 -> "GST percent cannot be negative."
        else -> null
    }

    private fun isValidEmail(email: String): Boolean =
        android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

    private fun getCurrentTimeString(): String =
        java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            .format(java.util.Date())

    // ─────────────────────────────────────────────────────────────────────
    // Factory
    // ─────────────────────────────────────────────────────────────────────

    companion object {
        fun provideFactory(
            settingsRepository: SettingsRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(settingsRepository) as T
            }
        }
    }
}
