package com.example.propaintersplastererspayment.feature.invoice.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.propaintersplastererspayment.data.local.model.JobWithInvoices
import com.example.propaintersplastererspayment.domain.repository.JobRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class QuickInvoiceListUiState(
    val invoices: List<JobWithInvoices> = emptyList(),
    val searchQuery: String = ""
)

class QuickInvoiceListViewModel(
    private val jobRepository: JobRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<QuickInvoiceListUiState> = combine(
        jobRepository.observeQuickInvoicesWithInvoices(),
        _searchQuery
    ) { invoices, query ->
        val filtered = if (query.isBlank()) {
            invoices
        } else {
            invoices.filter {
                it.job.clientNameSnapshot.contains(query, ignoreCase = true) ||
                it.job.propertyAddress.contains(query, ignoreCase = true) ||
                (it.invoices.firstOrNull()?.invoiceNumber?.contains(query, ignoreCase = true) ?: false)
            }
        }
        QuickInvoiceListUiState(invoices = filtered, searchQuery = query)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = QuickInvoiceListUiState()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun deleteQuickInvoice(jobId: Long) {
        viewModelScope.launch {
            jobRepository.deleteJobById(jobId)
        }
    }

    companion object {
        fun provideFactory(
            jobRepository: JobRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return QuickInvoiceListViewModel(jobRepository) as T
            }
        }
    }
}
