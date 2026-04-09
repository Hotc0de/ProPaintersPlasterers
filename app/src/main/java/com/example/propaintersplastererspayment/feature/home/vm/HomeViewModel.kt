package com.example.propaintersplastererspayment.feature.home.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.propaintersplastererspayment.data.local.entity.JobEntity
import com.example.propaintersplastererspayment.domain.repository.JobRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val jobs: List<JobEntity> = emptyList(),
    val isLoading: Boolean = true,
    val userMessage: String? = null
)

class HomeViewModel(
    private val jobRepository: JobRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = jobRepository.observeJobs()
        .map { jobs -> HomeUiState(jobs = jobs, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState()
        )

    fun deleteJob(job: JobEntity) {
        viewModelScope.launch {
            jobRepository.deleteJob(job)
        }
    }

    companion object {
        fun provideFactory(jobRepository: JobRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(jobRepository) as T
                }
            }
    }
}

