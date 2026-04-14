package com.example.propaintersplastererspayment.feature.home.vm

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.propaintersplastererspayment.data.local.entity.JobEntity
import com.example.propaintersplastererspayment.data.local.model.JobWithInvoices
import com.example.propaintersplastererspayment.domain.repository.JobRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val jobs: List<JobWithInvoices> = emptyList(),
    val searchQuery: TextFieldValue = TextFieldValue(""),
    val isLoading: Boolean = true,
    val userMessage: String? = null
)

data class JobTimelineState(
    val startDate: String,
    val finishDate: String,
    val rawStartDate: Long?,
    val rawFinishDate: Long?
)

class HomeViewModel(
    private val jobRepository: JobRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow(TextFieldValue(""))

    val uiState: StateFlow<HomeUiState> = combine(
        jobRepository.observeJobsWithInvoices(),
        _searchQuery
    ) { jobs, query ->
        val filteredJobs = if (query.text.isBlank()) {
            jobs
        } else {
            val searchText = query.text.lowercase()
            jobs.filter { jobWithInvoices ->
                val job = jobWithInvoices.job
                val invoiceNumber = jobWithInvoices.invoices.firstOrNull()?.invoiceNumber?.lowercase() ?: ""
                
                job.clientName.lowercase().contains(searchText) ||
                        job.propertyAddress.lowercase().contains(searchText) ||
                        invoiceNumber.contains(searchText) ||
                        job.jobName.lowercase().contains(searchText)
            }
        }
        HomeUiState(
            jobs = filteredJobs,
            searchQuery = query,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    fun onSearchQueryChange(newQuery: TextFieldValue) {
        _searchQuery.value = newQuery
    }

    fun updateJobDates(jobId: Long, startDate: Long?, finishDate: Long?) {
        viewModelScope.launch {
            jobRepository.updateJobDates(jobId, startDate, finishDate)
        }
    }

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

