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
    val isLoading: Boolean = false
)

data class PaymentWithClient(
    val payment: PaymentEntity,
    val client: ClientEntity?
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
        PaymentUiState(payments = paymentWithClients)
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

    fun addPayment(clientId: Long, amount: Double, notes: String) {
        viewModelScope.launch {
            paymentRepository.insertPayment(
                PaymentEntity(
                    clientId = clientId,
                    amount = amount,
                    notes = notes
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
