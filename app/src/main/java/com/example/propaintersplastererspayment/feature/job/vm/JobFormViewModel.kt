package com.example.propaintersplastererspayment.feature.job.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.propaintersplastererspayment.data.local.entity.JobEntity
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
    val clientName: String = "",
    val notes: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false
)

class JobFormViewModel(
    private val jobId: Long?,
    private val jobRepository: JobRepository
) : ViewModel() {

    private val mutableUiState = MutableStateFlow(JobFormUiState())
    val uiState: StateFlow<JobFormUiState> = mutableUiState.asStateFlow()

    init {
        if (jobId == null) {
            mutableUiState.update { it.copy(isLoading = false) }
        } else {
            viewModelScope.launch {
                jobRepository.observeJob(jobId)
                    .first()
                    ?.let { job ->
                        mutableUiState.update {
                            it.copy(
                                jobId = job.jobId,
                                address = job.propertyAddress,
                                clientName = if (job.clientName.isNotBlank()) job.clientName else job.jobName,
                                notes = job.notes,
                                isLoading = false
                            )
                        }
                    } ?: mutableUiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onAddressChange(value: String) {
        mutableUiState.update { it.copy(address = value, errorMessage = null) }
    }

    fun onClientNameChange(value: String) {
        mutableUiState.update { it.copy(clientName = value, errorMessage = null) }
    }

    fun onNotesChange(value: String) {
        mutableUiState.update { it.copy(notes = value, errorMessage = null) }
    }

    fun saveJob() {
        val current = mutableUiState.value
        val validationError = when {
            current.address.isBlank() -> "Address is required."
            current.clientName.isBlank() -> "Client/Company name is required."
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
                    clientName = current.clientName.trim(),
                    jobName = current.clientName.trim(),
                    notes = current.notes.trim()
                )
            )
            mutableUiState.update { it.copy(isSaving = false, saveSuccess = true) }
        }
    }

    companion object {
        fun provideFactory(
            jobId: Long?,
            jobRepository: JobRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return JobFormViewModel(jobId, jobRepository) as T
            }
        }
    }
}



