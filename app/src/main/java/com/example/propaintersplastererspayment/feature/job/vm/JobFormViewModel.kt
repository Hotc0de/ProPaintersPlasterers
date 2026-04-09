package com.example.propaintersplastererspayment.feature.job.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.propaintersplastererspayment.data.local.entity.ClientEntity
import com.example.propaintersplastererspayment.data.local.entity.JobEntity
import com.example.propaintersplastererspayment.domain.repository.ClientRepository
import com.example.propaintersplastererspayment.domain.repository.JobRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class JobFormUiState(
    val jobId: Long? = null,
    val address: String = "",
    val clientQuery: String = "",
    val selectedClientId: Long? = null,
    val selectedClientName: String = "",
    val clients: List<ClientEntity> = emptyList(),
    val notes: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false
) {
    val filteredClients: List<ClientEntity>
        get() {
            val q = clientQuery.trim()
            // Show all clients when: query is empty, OR query equals the already-selected
            // client name (dropdown opened for browsing, not for a new search).
            if (q.isEmpty() || q.equals(selectedClientName, ignoreCase = true)) return clients
            return clients.filter { it.name.contains(q, ignoreCase = true) }
        }
}

class JobFormViewModel(
    private val jobId: Long?,
    private val jobRepository: JobRepository,
    private val clientRepository: ClientRepository
) : ViewModel() {

    private val mutableUiState = MutableStateFlow(JobFormUiState())
    val uiState: StateFlow<JobFormUiState> = mutableUiState.asStateFlow()

    init {
        observeClients()
        loadJobIfEditing()
    }

    private fun observeClients() {
        viewModelScope.launch {
            clientRepository.observeClients().collect { clients ->
                mutableUiState.update { state ->
                    val selectedClient = state.selectedClientId?.let { selectedId ->
                        clients.firstOrNull { it.clientId == selectedId }
                    }
                    state.copy(
                        clients = clients,
                        selectedClientName = selectedClient?.name ?: state.selectedClientName
                    )
                }
            }
        }
    }

    private fun loadJobIfEditing() {
        if (jobId == null) {
            mutableUiState.update { it.copy(isLoading = false) }
            return
        }

        viewModelScope.launch {
            val job = jobRepository.observeJob(jobId).first()
            if (job != null) {
                val selectedClient = job.clientId?.let { clientRepository.getClient(it) }
                val selectedName = selectedClient?.name
                    ?: job.clientNameSnapshot
                    .ifBlank { job.jobName }
                mutableUiState.update {
                    it.copy(
                        jobId = job.jobId,
                        address = job.propertyAddress,
                        selectedClientId = job.clientId,
                        selectedClientName = selectedName,
                        clientQuery = selectedName,
                        notes = job.notes,
                        isLoading = false
                    )
                }
            } else {
                mutableUiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onAddressChange(value: String) {
        mutableUiState.update { it.copy(address = value, errorMessage = null) }
    }

    fun onClientQueryChange(value: String) {
        mutableUiState.update {
            it.copy(
                clientQuery = value,
                // Typing means selection may have changed; force explicit reselection.
                selectedClientId = null,
                selectedClientName = "",
                errorMessage = null
            )
        }
    }

    fun onClientSelected(client: ClientEntity) {
        mutableUiState.update {
            it.copy(
                selectedClientId = client.clientId,
                selectedClientName = client.name,
                clientQuery = client.name,
                errorMessage = null
            )
        }
    }

    fun onClientAdded(clientId: Long) {
        viewModelScope.launch {
            val newClient = clientRepository.getClient(clientId) ?: return@launch
            mutableUiState.update {
                it.copy(
                    selectedClientId = newClient.clientId,
                    selectedClientName = newClient.name,
                    clientQuery = newClient.name,
                    errorMessage = null
                )
            }
        }
    }

    fun onNotesChange(value: String) {
        mutableUiState.update { it.copy(notes = value, errorMessage = null) }
    }

    fun saveJob() {
        val current = mutableUiState.value
        val validationError = when {
            current.address.isBlank() -> "Address is required."
            current.selectedClientId == null -> "Please select a client/company."
            else -> null
        }
        if (validationError != null) {
            mutableUiState.update { it.copy(errorMessage = validationError) }
            return
        }

        viewModelScope.launch {
            mutableUiState.update { it.copy(isSaving = true) }
            jobRepository.saveJob(
                JobEntity(
                    jobId = current.jobId ?: 0,
                    propertyAddress = current.address.trim(),
                    clientId = current.selectedClientId,
                    clientNameSnapshot = current.selectedClientName.trim(),
                    jobName = current.selectedClientName.trim(),
                    notes = current.notes.trim(),
                    createdAt = current.jobId?.let { existing ->
                        jobRepository.observeJob(existing).first()?.createdAt
                    } ?: System.currentTimeMillis()
                )
            )
            mutableUiState.update { it.copy(isSaving = false, saveSuccess = true) }
        }
    }

    companion object {
        fun provideFactory(
            jobId: Long?,
            jobRepository: JobRepository,
            clientRepository: ClientRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return JobFormViewModel(jobId, jobRepository, clientRepository) as T
            }
        }
    }
}
