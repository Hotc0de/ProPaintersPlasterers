package com.example.propaintersplastererspayment.feature.settings.vm

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.propaintersplastererspayment.core.util.BankAccountFormatUtils
import com.example.propaintersplastererspayment.core.util.PhoneFormatUtils
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
    val businessName: TextFieldValue = TextFieldValue(""),
    val address: TextFieldValue = TextFieldValue(""),
    val phoneNumber: TextFieldValue = TextFieldValue(""),
    val email: TextFieldValue = TextFieldValue(""),
    val gstNumber: TextFieldValue = TextFieldValue(""),
    val bankAccountNumber: TextFieldValue = TextFieldValue(""),
    val bankName: TextFieldValue = TextFieldValue(""),
    val defaultLabourRateText: TextFieldValue = TextFieldValue(""),
    val defaultGstPercentText: TextFieldValue = TextFieldValue("15"),
    val gstEnabledByDefault: Boolean = true,
    val errorMessage: String? = null
) {
    val parsedLabourRate: Double?
        get() = defaultLabourRateText.text.trim().replace(",", "").toDoubleOrNull()

    val parsedGstPercent: Double?
        get() = defaultGstPercentText.text.trim().replace(",", "").toDoubleOrNull()

    val isValid: Boolean
        get() = businessName.text.isNotBlank() &&
                address.text.isNotBlank() &&
                phoneNumber.text.isNotBlank() &&
                email.text.isNotBlank() &&
                bankAccountNumber.text.isNotBlank() &&
                parsedLabourRate != null &&
                parsedLabourRate!! > 0 &&
                parsedGstPercent != null &&
                parsedGstPercent!! >= 0

    val bankAccountFormatError: String?
        get() = if (
            bankAccountNumber.text.isNotBlank() &&
            !BankAccountFormatUtils.isValid(bankAccountNumber.text)
        ) {
            "Use format 00-0000-0000000-00"
        } else {
            null
        }

    val phoneFormatError: String?
        get() = if (phoneNumber.text.isNotBlank() && !PhoneFormatUtils.isValid(phoneNumber.text)) {
            "Use format 000-0000000"
        } else {
            null
        }
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
            if (firstSettings != null && formState.value.businessName.text.isEmpty()) {
                val phoneNumber = PhoneFormatUtils.formatInput(firstSettings.phoneNumber)
                val bankAccount = BankAccountFormatUtils.formatInput(firstSettings.bankAccountNumber)
                val labourRate = if (firstSettings.defaultLabourRate > 0) {
                    firstSettings.defaultLabourRate.toString()
                } else {
                    ""
                }
                val gstPercent = (firstSettings.defaultGstRate * 100).toInt().toString()

                formState.value = SettingsFormState(
                    businessName = TextFieldValue(firstSettings.businessName),
                    address = TextFieldValue(firstSettings.address),
                    phoneNumber = TextFieldValue(phoneNumber, selection = androidx.compose.ui.text.TextRange(phoneNumber.length)),
                    email = TextFieldValue(firstSettings.email),
                    gstNumber = TextFieldValue(firstSettings.gstNumber),
                    bankAccountNumber = TextFieldValue(bankAccount, selection = androidx.compose.ui.text.TextRange(bankAccount.length)),
                    bankName = TextFieldValue(firstSettings.bankName),
                    defaultLabourRateText = TextFieldValue(labourRate, selection = androidx.compose.ui.text.TextRange(labourRate.length)),
                    defaultGstPercentText = TextFieldValue(gstPercent, selection = androidx.compose.ui.text.TextRange(gstPercent.length)),
                    gstEnabledByDefault = firstSettings.gstEnabledByDefault
                )
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Form field updates
    // ─────────────────────────────────────────────────────────────────────

    fun onBusinessNameChange(value: TextFieldValue) {
        formState.update { it.copy(businessName = value, errorMessage = null) }
    }

    fun onAddressChange(value: TextFieldValue) {
        formState.update { it.copy(address = value, errorMessage = null) }
    }

    fun onPhoneNumberChange(value: TextFieldValue) {
        val formatted = PhoneFormatUtils.formatInput(value.text)
        val selectionOffset = if (value.text.length != formatted.length && value.selection.collapsed) {
            val diff = formatted.length - value.text.length
            (value.selection.start + diff).coerceIn(0, formatted.length)
        } else {
            value.selection.start.coerceAtMost(formatted.length)
        }
        formState.update { it.copy(
            phoneNumber = value.copy(text = formatted, selection = androidx.compose.ui.text.TextRange(selectionOffset)),
            errorMessage = null
        ) }
    }

    fun onEmailChange(value: TextFieldValue) {
        formState.update { it.copy(email = value, errorMessage = null) }
    }

    fun onGstNumberChange(value: TextFieldValue) {
        formState.update { it.copy(gstNumber = value, errorMessage = null) }
    }

    fun onBankAccountNumberChange(value: TextFieldValue) {
        val formatted = BankAccountFormatUtils.formatInput(value.text)
        val selectionOffset = if (value.text.length != formatted.length && value.selection.collapsed) {
            val diff = formatted.length - value.text.length
            (value.selection.start + diff).coerceIn(0, formatted.length)
        } else {
            value.selection.start.coerceAtMost(formatted.length)
        }
        formState.update { it.copy(
            bankAccountNumber = value.copy(text = formatted, selection = androidx.compose.ui.text.TextRange(selectionOffset)),
            errorMessage = null
        ) }
    }

    fun onBankNameChange(value: TextFieldValue) {
        formState.update { it.copy(bankName = value, errorMessage = null) }
    }

    fun onDefaultLabourRateChange(value: TextFieldValue) {
        formState.update { it.copy(defaultLabourRateText = value, errorMessage = null) }
    }

    fun onDefaultGstPercentChange(value: TextFieldValue) {
        formState.update { it.copy(defaultGstPercentText = value, errorMessage = null) }
    }

    fun onGstEnabledChange(value: Boolean) {
        formState.update { it.copy(gstEnabledByDefault = value) }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Save action
    // ─────────────────────────────────────────────────────────────────────

    fun saveSettings() {
        val current = formState.value
        val error = validateForm(current)
        if (error != null) {
            formState.update { it.copy(errorMessage = error) }
            return
        }

        isSaving.value = true

        viewModelScope.launch {
            try {
                settingsRepository.saveSettings(
                    AppSettingsEntity(
                        settingsId = 1,
                        businessName = current.businessName.text.trim(),
                        address = current.address.text.trim(),
                        phoneNumber = current.phoneNumber.text.trim(),
                        email = current.email.text.trim(),
                        gstNumber = current.gstNumber.text.trim(),
                        bankAccountNumber = current.bankAccountNumber.text.trim(),
                        bankName = current.bankName.text.trim(),
                        defaultLabourRate = current.parsedLabourRate ?: 0.0,
                        defaultGstRate = (current.parsedGstPercent ?: 0.0) / 100.0,
                        gstEnabledByDefault = current.gstEnabledByDefault
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
        form.businessName.text.isBlank() -> "Business name is required."
        form.address.text.isBlank() -> "Address is required."
        form.phoneNumber.text.isBlank() -> "Phone number is required."
        !PhoneFormatUtils.isValid(form.phoneNumber.text) -> "Phone number must match 000-0000000."
        form.email.text.isBlank() -> "Email is required."
        !isValidEmail(form.email.text) -> "Please enter a valid email address."
        form.bankAccountNumber.text.isBlank() -> "Bank account number is required."
        !BankAccountFormatUtils.isValid(form.bankAccountNumber.text) ->
            "Bank account must match 00-0000-0000000-00."
        form.defaultLabourRateText.text.isBlank() -> "Default labour rate is required."
        form.parsedLabourRate == null -> "Labour rate must be a valid number."
        form.parsedLabourRate!! <= 0 -> "Labour rate must be greater than 0."
        form.defaultGstPercentText.text.isBlank() -> "Default GST percent is required."
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
