package com.example.propaintersplastererspayment.feature.payment.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.propaintersplastererspayment.data.local.entity.ClientEntity
import com.example.propaintersplastererspayment.data.local.entity.PaymentEntity
import com.example.propaintersplastererspayment.domain.repository.ClientRepository
import com.example.propaintersplastererspayment.domain.repository.PaymentRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PaymentUiState(
    val payments: List<PaymentWithClient> = emptyList(),
    val debtClients: List<ClientDebtSummary> = emptyList(),
    val isLoading: Boolean = false
)

data class PaymentWithClient(
    val payment: PaymentEntity,
    val client: ClientEntity?
)

data class ClientDebtSummary(
    val client: ClientEntity,
    val totalDebt: Double,
    val totalPaid: Double,
    val outstanding: Double
)

class PaymentViewModel(
    private val paymentRepository: PaymentRepository,
    private val clientRepository: ClientRepository
) : ViewModel() {

    val uiState: StateFlow<PaymentUiState> = combine(
        paymentRepository.getAllPaymentsStream(),
        clientRepository.observeClients()
    ) { payments, clients ->
        val clientMap = clients.associateBy { it.clientId }
        val paymentWithClients = payments.map { payment ->
            PaymentWithClient(payment, clientMap[payment.clientId])
        }
        val paymentsByClient = payments.groupBy { it.clientId }
        val debtClients = clients
            .map { client ->
                val totalPaid = paymentsByClient[client.clientId].orEmpty().sumOf { it.amount }
                ClientDebtSummary(
                    client = client,
                    totalDebt = client.manualTotalDebt,
                    totalPaid = totalPaid,
                    outstanding = client.manualTotalDebt - totalPaid
                )
            }
            .filter { it.totalDebt > 0.0 || it.totalPaid > 0.0 }
            .sortedBy { it.client.name.lowercase() }

        PaymentUiState(
            payments = paymentWithClients,
            debtClients = debtClients
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PaymentUiState(isLoading = true)
    )

    val clients: StateFlow<List<ClientEntity>> = clientRepository.observeClients()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun addPayment(
        clientId: Long,
        amount: Double,
        notes: String,
        date: Long = System.currentTimeMillis(),
        reference: String = "",
        details: String = ""
    ) {
        viewModelScope.launch {
            paymentRepository.insertPayment(
                PaymentEntity(
                    clientId = clientId,
                    amount = amount,
                    date = date,
                    notes = notes,
                    reference = reference,
                    details = details
                )
            )
        }
    }

    fun addNewClientAndPayment(clientName: String, amount: Double, notes: String) {
        viewModelScope.launch {
            val clientId = clientRepository.saveClient(ClientEntity(name = clientName))
            paymentRepository.insertPayment(
                PaymentEntity(
                    clientId = clientId,
                    amount = amount,
                    notes = notes
                )
            )
        }
    }

    fun updatePayment(payment: PaymentEntity) {
        viewModelScope.launch {
            paymentRepository.updatePayment(payment)
        }
    }

    fun deletePayment(payment: PaymentEntity) {
        viewModelScope.launch {
            paymentRepository.deletePayment(payment)
        }
    }

    fun saveClientDebt(clientId: Long?, newClientName: String?, totalDebt: Double) {
        viewModelScope.launch {
            if (clientId != null) {
                clientRepository.getClient(clientId)?.let { client ->
                    clientRepository.saveClient(client.copy(manualTotalDebt = totalDebt))
                }
            } else if (!newClientName.isNullOrBlank()) {
                clientRepository.saveClient(
                    ClientEntity(
                        name = newClientName.trim(),
                        manualTotalDebt = totalDebt
                    )
                )
            }
        }
    }

    fun updateClientDebt(client: ClientEntity, totalDebt: Double) {
        viewModelScope.launch {
            clientRepository.saveClient(client.copy(manualTotalDebt = totalDebt))
        }
    }

    fun clearClientDebt(client: ClientEntity) {
        viewModelScope.launch {
            clientRepository.saveClient(client.copy(manualTotalDebt = 0.0))
            paymentRepository.deletePaymentsForClient(client.clientId)
        }
    }

    companion object {
        fun provideFactory(
            paymentRepository: PaymentRepository,
            clientRepository: ClientRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PaymentViewModel(paymentRepository, clientRepository) as T
            }
        }
    }
}
