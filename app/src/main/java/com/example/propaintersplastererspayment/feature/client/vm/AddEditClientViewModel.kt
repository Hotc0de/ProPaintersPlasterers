package com.example.propaintersplastererspayment.feature.client.vm

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.propaintersplastererspayment.core.util.PhoneFormatUtils
import com.example.propaintersplastererspayment.data.local.entity.ClientEntity
import com.example.propaintersplastererspayment.domain.repository.ClientRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ClientFormState(
    val name: TextFieldValue = TextFieldValue(""),
    val clientType: String = "PRIVATE",   // "PRIVATE" or "BUSINESS"
    val address: TextFieldValue = TextFieldValue(""),
    val phoneNumber: TextFieldValue = TextFieldValue(""),
    val email: TextFieldValue = TextFieldValue(""),
    val notes: TextFieldValue = TextFieldValue(""),
    val errorMessage: String? = null
) {
    val isValid: Boolean get() = name.text.isNotBlank()

    val phoneFormatError: String?
        get() = if (phoneNumber.text.isNotBlank() && !PhoneFormatUtils.isValid(phoneNumber.text)) {
            "Use format 000-0000000"
        } else {
            null
        }
}

data class AddEditClientUiState(
    val formState: ClientFormState = ClientFormState(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isExistingClient: Boolean = false,
    val userMessage: String? = null
)

class AddEditClientViewModel(
    private val clientId: Long?,
    private val clientRepository: ClientRepository
) : ViewModel() {

    private val formState = MutableStateFlow(ClientFormState())
    private val isSaving = MutableStateFlow(false)
    private val userMessage = MutableStateFlow<String?>(null)
    private val isExistingClient = MutableStateFlow(false)

    private val _savedEvent = MutableSharedFlow<Long>()
    val savedEvent: SharedFlow<Long> = _savedEvent.asSharedFlow()

    val uiState: StateFlow<AddEditClientUiState> = combine(
        formState, isSaving, userMessage, isExistingClient
    ) { form, saving, message, existing ->
        AddEditClientUiState(
            formState = form,
            isSaving = saving,
            isExistingClient = existing,
            userMessage = message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AddEditClientUiState()
    )

    init {
        if (clientId != null && clientId > 0L) {
            viewModelScope.launch {
                val client = clientRepository.getClient(clientId)
                if (client != null) {
                    isExistingClient.value = true
                    formState.value = ClientFormState(
                        name = TextFieldValue(client.name),
                        clientType = client.clientType,
                        address = TextFieldValue(client.address),
                        phoneNumber = TextFieldValue(PhoneFormatUtils.formatInput(client.phoneNumber)),
                        email = TextFieldValue(client.email),
                        notes = TextFieldValue(client.notes)
                    )
                }
            }
        }
    }

    fun onNameChange(value: TextFieldValue) = formState.update { it.copy(name = value, errorMessage = null) }
    fun onClientTypeChange(value: String) = formState.update { it.copy(clientType = value) }
    fun onAddressChange(value: TextFieldValue) = formState.update { it.copy(address = value) }
    fun onPhoneChange(value: TextFieldValue) {
        val formatted = PhoneFormatUtils.formatInput(value.text)
        val diff = formatted.length - value.text.length
        val newCursor = (value.selection.start + diff).coerceIn(0, formatted.length)
        formState.update {
            it.copy(
                phoneNumber = value.copy(
                    text = formatted,
                    selection = TextRange(newCursor)
                )
            )
        }
    }
    fun onEmailChange(value: TextFieldValue) = formState.update { it.copy(email = value) }
    fun onNotesChange(value: TextFieldValue) = formState.update { it.copy(notes = value) }

    fun saveClient() {
        val form = formState.value
        if (form.name.text.isBlank()) {
            formState.update { it.copy(errorMessage = "Name is required.") }
            return
        }
        if (form.phoneNumber.text.isNotBlank() && !PhoneFormatUtils.isValid(form.phoneNumber.text)) {
            formState.update { it.copy(errorMessage = "Phone number must match 000-0000000.") }
            return
        }
        isSaving.value = true
        viewModelScope.launch {
            try {
                val savedClientId = clientRepository.saveClient(
                    ClientEntity(
                        clientId = clientId ?: 0L,
                        name = form.name.text.trim(),
                        clientType = form.clientType,
                        address = form.address.text.trim(),
                        phoneNumber = form.phoneNumber.text.trim(),
                        email = form.email.text.trim(),
                        notes = form.notes.text.trim()
                    )
                )
                _savedEvent.emit(savedClientId)
            } catch (e: Exception) {
                userMessage.value = "Error saving client: ${e.message}"
            } finally {
                isSaving.value = false
            }
        }
    }

    fun deleteClient() {
        val id = clientId ?: return
        viewModelScope.launch {
            val client = clientRepository.getClient(id) ?: return@launch
            clientRepository.deleteClient(client)
            _savedEvent.emit(-1L)
        }
    }

    fun clearMessage() { userMessage.value = null }

    companion object {
        fun provideFactory(
            clientId: Long?,
            clientRepository: ClientRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                AddEditClientViewModel(clientId, clientRepository) as T
        }
    }
}

