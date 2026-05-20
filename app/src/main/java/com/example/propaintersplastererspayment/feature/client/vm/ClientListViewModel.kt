package com.example.propaintersplastererspayment.feature.client.vm

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.propaintersplastererspayment.data.local.entity.ClientEntity
import com.example.propaintersplastererspayment.domain.repository.ClientRepository
import com.example.propaintersplastererspayment.domain.repository.InvoiceRepository
import com.example.propaintersplastererspayment.domain.repository.JobRepository
import com.example.propaintersplastererspayment.domain.repository.PaymentRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ClientWithBalance(
    val client: ClientEntity,
    val balance: Double = 0.0
)

data class ClientListUiState(
    val clients: List<ClientWithBalance> = emptyList(),
    val searchQuery: TextFieldValue = TextFieldValue(""),
    val isLoading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
class ClientListViewModel(
    private val clientRepository: ClientRepository,
    private val paymentRepository: PaymentRepository,
    private val invoiceRepository: InvoiceRepository,
    private val jobRepository: JobRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow(TextFieldValue(""))

    private val filteredClients = searchQuery
        .map { it.text }
        .flatMapLatest { query ->
            if (query.isBlank()) {
                clientRepository.observeClients()
            } else {
                clientRepository.observeSuggestions(query)
            }
        }

    val uiState: StateFlow<ClientListUiState> = combine(
        filteredClients,
        paymentRepository.getAllPaymentsStream(),
        jobRepository.observeJobsWithInvoices(),
        searchQuery
    ) { clients, allPayments, allJobsWithInvoices, query ->
        val clientWithBalances = clients.map { client ->
            val clientPayments = allPayments.filter { it.clientId == client.clientId }.sumOf { it.amount }
            
            val clientInvoices = allJobsWithInvoices
                .filter { it.job.clientId == client.clientId }
                .flatMap { it.invoices }
                .sumOf { it.totalAmount }

            ClientWithBalance(
                client = client,
                balance = clientInvoices - clientPayments
            )
        }

        ClientListUiState(
            clients = clientWithBalances,
            searchQuery = query,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ClientListUiState()
    )

    fun onSearchQueryChange(query: TextFieldValue) {
        searchQuery.update { query }
    }

    fun deleteClient(client: ClientEntity) {
        viewModelScope.launch {
            clientRepository.deleteClient(client)
        }
    }

    companion object {
        fun provideFactory(
            clientRepository: ClientRepository,
            paymentRepository: PaymentRepository,
            invoiceRepository: InvoiceRepository,
            jobRepository: JobRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ClientListViewModel(
                        clientRepository,
                        paymentRepository,
                        invoiceRepository,
                        jobRepository
                    ) as T
            }
    }
}

