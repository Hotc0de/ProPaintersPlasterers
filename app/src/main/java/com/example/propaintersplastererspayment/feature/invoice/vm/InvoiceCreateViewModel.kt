package com.example.propaintersplastererspayment.feature.invoice.vm

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.propaintersplastererspayment.core.util.DateFormatUtils
import com.example.propaintersplastererspayment.core.util.InvoiceUtils
import com.example.propaintersplastererspayment.data.local.entity.ClientEntity
import com.example.propaintersplastererspayment.data.local.entity.InvoiceEntity
import com.example.propaintersplastererspayment.data.local.entity.InvoiceLineEntity
import com.example.propaintersplastererspayment.data.local.entity.JobEntity
import com.example.propaintersplastererspayment.data.local.entity.JobStatus
import com.example.propaintersplastererspayment.domain.repository.ClientRepository
import com.example.propaintersplastererspayment.domain.repository.InvoiceRepository
import com.example.propaintersplastererspayment.domain.repository.JobRepository
import com.example.propaintersplastererspayment.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class InvoiceCreateUiState(
    val clientMode: ClientMode = ClientMode.Existing,
    val existingClients: List<ClientEntity> = emptyList(),
    val selectedClient: ClientEntity? = null,
    val newClientName: TextFieldValue = TextFieldValue(""),
    val address: TextFieldValue = TextFieldValue(""),
    val invoiceNumber: String = "",
    val description: TextFieldValue = TextFieldValue("Labour & Materials"),
    val qty: TextFieldValue = TextFieldValue("1"),
    val rate: TextFieldValue = TextFieldValue(""),
    val includeGst: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val successJobId: Long? = null
) {
    val subtotal: Double
        get() = (qty.text.toDoubleOrNull() ?: 0.0) * (rate.text.toDoubleOrNull() ?: 0.0)

    val gstAmount: Double
        get() = if (includeGst) subtotal * InvoiceUtils.DEFAULT_GST_RATE else 0.0

    val total: Double
        get() = subtotal + gstAmount
}

enum class ClientMode { Existing, New }

class InvoiceCreateViewModel(
    private val clientRepository: ClientRepository,
    private val jobRepository: JobRepository,
    private val invoiceRepository: InvoiceRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvoiceCreateUiState())
    val uiState: StateFlow<InvoiceCreateUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val clients = clientRepository.observeClients().first()
            val nextInvoiceNumber = invoiceRepository.generateUniqueInvoiceNumber()
            val settings = settingsRepository.observeSettings().first()
            _uiState.update { it.copy(
                existingClients = clients,
                invoiceNumber = nextInvoiceNumber,
                includeGst = settings?.gstEnabledByDefault ?: true
            ) }
        }
    }

    fun onClientModeChange(mode: ClientMode) {
        _uiState.update { it.copy(clientMode = mode, selectedClient = null, newClientName = TextFieldValue(""), address = TextFieldValue("")) }
    }

    fun onClientSelected(client: ClientEntity) {
        _uiState.update { it.copy(selectedClient = client, address = TextFieldValue(client.address)) }
    }

    fun onNewClientNameChange(value: TextFieldValue) {
        _uiState.update { it.copy(newClientName = value) }
    }

    fun onAddressChange(value: TextFieldValue) {
        _uiState.update { it.copy(address = value) }
    }

    fun onDescriptionChange(value: TextFieldValue) {
        _uiState.update { it.copy(description = value) }
    }

    fun onQtyChange(value: TextFieldValue) {
        _uiState.update { it.copy(qty = value) }
    }

    fun onRateChange(value: TextFieldValue) {
        _uiState.update { it.copy(rate = value) }
    }

    fun onIncludeGstChange(value: Boolean) {
        _uiState.update { it.copy(includeGst = value) }
    }

    fun saveInvoice() {
        val state = _uiState.value
        val clientName = if (state.clientMode == ClientMode.Existing) state.selectedClient?.name else state.newClientName.text
        
        if (clientName.isNullOrBlank()) {
            _uiState.update { it.copy(error = "Please select or enter a client name.") }
            return
        }
        if (state.address.text.isBlank()) {
            _uiState.update { it.copy(error = "Please enter an address.") }
            return
        }
        if (state.rate.text.toDoubleOrNull() == null) {
            _uiState.update { it.copy(error = "Please enter a valid rate.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                // 1. Get or Create Client
                val clientId = if (state.clientMode == ClientMode.Existing) {
                    state.selectedClient?.clientId!!
                } else {
                    clientRepository.saveClient(ClientEntity(name = clientName.trim(), address = state.address.text.trim()))
                }

                // 2. Create Job
                val jobName = "Invoice: $clientName"
                val jobId = jobRepository.saveJob(JobEntity(
                    clientId = clientId,
                    jobName = jobName,
                    propertyAddress = state.address.text.trim(),
                    status = JobStatus.WAITING_FOR_PAYMENT,
                    clientNameSnapshot = clientName.trim(),
                    isQuickInvoice = true
                ))

                // 3. Create Invoice
                val now = System.currentTimeMillis()
                val settings = settingsRepository.observeSettings().first()
                val gstRate = settings?.defaultGstRate ?: InvoiceUtils.DEFAULT_GST_RATE
                val subtotal = state.subtotal
                val gstAmount = if (state.includeGst) subtotal * gstRate else 0.0

                val invoiceId = invoiceRepository.saveInvoice(InvoiceEntity(
                    jobId = jobId,
                    clientId = clientId,
                    invoiceNumber = state.invoiceNumber,
                    invoiceDate = DateFormatUtils.toStoredDate(DateFormatUtils.todayDisplayDate()) ?: "",
                    billToNameSnapshot = clientName.trim(),
                    billToAddressSnapshot = state.address.text.trim(),
                    billToPhoneSnapshot = if (state.clientMode == ClientMode.Existing) state.selectedClient?.phoneNumber.orEmpty() else "",
                    billToEmailSnapshot = if (state.clientMode == ClientMode.Existing) state.selectedClient?.email.orEmpty() else "",
                    subtotalExclusiveGst = subtotal,
                    gstEnabled = state.includeGst,
                    gstRate = gstRate,
                    gstAmount = gstAmount,
                    totalAmount = subtotal + gstAmount,
                    notes = "",
                    createdAt = now,
                    updatedAt = now
                ))

                // 4. Create Line Item
                invoiceRepository.saveInvoiceLine(InvoiceLineEntity(
                    invoiceId = invoiceId,
                    description = state.description.text.trim(),
                    qty = state.qty.text.toDoubleOrNull() ?: 1.0,
                    rate = state.rate.text.toDoubleOrNull() ?: 0.0,
                    amount = subtotal,
                    manualAmountOverride = false,
                    sortOrder = 0
                ))

                _uiState.update { it.copy(isSaving = false, successJobId = jobId) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message ?: "Failed to save invoice") }
            }
        }
    }

    companion object {
        fun provideFactory(
            clientRepository: ClientRepository,
            jobRepository: JobRepository,
            invoiceRepository: InvoiceRepository,
            settingsRepository: SettingsRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return InvoiceCreateViewModel(clientRepository, jobRepository, invoiceRepository, settingsRepository) as T
            }
        }
    }
}
