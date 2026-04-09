package com.example.propaintersplastererspayment.feature.client.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
    val name: String = "",
    val clientType: String = "PRIVATE",   // "PRIVATE" or "BUSINESS"
    val address: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val notes: String = "",
    val errorMessage: String? = null
) {
    val isValid: Boolean get() = name.isNotBlank()
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

    private val _savedEvent = MutableSharedFlow<Unit>()
    val savedEvent: SharedFlow<Unit> = _savedEvent.asSharedFlow()

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
                        name = client.name,
                        clientType = client.clientType,
                        address = client.address,
                        phoneNumber = client.phoneNumber,
                        email = client.email,
                        notes = client.notes
                    )
                }
            }
        }
    }

    fun onNameChange(value: String) = formState.update { it.copy(name = value, errorMessage = null) }
    fun onClientTypeChange(value: String) = formState.update { it.copy(clientType = value) }
    fun onAddressChange(value: String) = formState.update { it.copy(address = value) }
    fun onPhoneChange(value: String) = formState.update { it.copy(phoneNumber = value) }
    fun onEmailChange(value: String) = formState.update { it.copy(email = value) }
    fun onNotesChange(value: String) = formState.update { it.copy(notes = value) }

    fun saveClient() {
        val form = formState.value
        if (form.name.isBlank()) {
            formState.update { it.copy(errorMessage = "Name is required.") }
            return
        }
        isSaving.value = true
        viewModelScope.launch {
            try {
                clientRepository.saveClient(
                    ClientEntity(
                        clientId = clientId ?: 0L,
                        name = form.name.trim(),
                        clientType = form.clientType,
                        address = form.address.trim(),
                        phoneNumber = form.phoneNumber.trim(),
                        email = form.email.trim(),
                        notes = form.notes.trim()
                    )
                )
                _savedEvent.emit(Unit)
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
            _savedEvent.emit(Unit)
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

