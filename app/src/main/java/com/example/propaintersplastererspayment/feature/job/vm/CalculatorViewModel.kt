package com.example.propaintersplastererspayment.feature.job.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.propaintersplastererspayment.core.pdf.*
import com.example.propaintersplastererspayment.core.util.DateFormatUtils
import com.example.propaintersplastererspayment.data.local.entity.AppSettingsEntity
import com.example.propaintersplastererspayment.domain.repository.JobRepository
import com.example.propaintersplastererspayment.domain.repository.SettingsRepository
import com.example.propaintersplastererspayment.feature.job.ui.CalculationItemUi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

data class CalculatorUiState(
    val items: List<CalculationItemUi> = listOf(CalculationItemUi(areaName = "Lounge", quantity = "25", costPerUnit = "15")),
    val userMessage: String? = null
)

class CalculatorViewModel(
    private val jobId: Long,
    private val jobRepository: JobRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    private val _pdfExportEvents = MutableSharedFlow<CalculationPdfData>()
    val pdfExportEvents: SharedFlow<CalculationPdfData> = _pdfExportEvents.asSharedFlow()

    fun addItem() {
        _uiState.update { it.copy(items = it.items + CalculationItemUi()) }
    }

    fun updateItem(updated: CalculationItemUi) {
        _uiState.update { state ->
            state.copy(items = state.items.map { if (it.id == updated.id) updated else it })
        }
    }

    fun deleteItem(id: String) {
        _uiState.update { state ->
            state.copy(items = state.items.filter { it.id != id })
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    fun exportPdf() {
        exportItemsPdf(_uiState.value.items)
    }

    fun exportSingleItemPdf(itemId: String) {
        val item = _uiState.value.items.find { it.id == itemId } ?: return
        exportItemsPdf(listOf(item))
    }

    private fun exportItemsPdf(items: List<CalculationItemUi>) {
        viewModelScope.launch {
            val job = jobRepository.observeJob(jobId).first()
            if (job == null) {
                _uiState.update { it.copy(userMessage = "Job not found.") }
                return@launch
            }

            val settings = settingsRepository.observeSettings().first() ?: AppSettingsEntity()

            val pdfItems = items.map { item ->
                val qty = item.quantity.toDoubleOrNull() ?: 0.0
                val cost = item.costPerUnit.toDoubleOrNull() ?: 0.0
                val subtotal = qty * cost
                val gstAmount = if (item.includeGst) subtotal * 0.15 else 0.0
                CalculationPdfRow(
                    areaName = item.areaName,
                    quantity = qty,
                    unit = if (item.isSqM) "m²" else "m",
                    costPerUnit = cost,
                    subtotal = subtotal,
                    includeGst = item.includeGst,
                    gstAmount = gstAmount,
                    total = subtotal + gstAmount
                )
            }

            val subtotal = pdfItems.sumOf { it.subtotal }
            val gstTotal = pdfItems.sumOf { it.gstAmount }
            val grandTotal = pdfItems.sumOf { it.total }

            val fileNamePrefix = if (items.size == 1) {
                "calculation-${items[0].areaName.replace(" ", "_")}"
            } else {
                "calculation-all"
            }

            val data = CalculationPdfData(
                fileName = "$fileNamePrefix-${job.jobId}-${System.currentTimeMillis()}.pdf",
                exportedAt = DateFormatUtils.todayDisplayDate(),
                business = settings.toBusinessDetails(),
                jobName = job.jobName,
                items = pdfItems,
                subtotal = subtotal,
                gstTotal = gstTotal,
                grandTotal = grandTotal
            )

            _pdfExportEvents.emit(data)
        }
    }

    fun onPdfExportFinished(success: Boolean) {
        _uiState.update { it.copy(userMessage = if (success) "PDF exported successfully" else "Failed to export PDF") }
    }

    companion object {
        fun provideFactory(
            jobId: Long,
            jobRepository: JobRepository,
            settingsRepository: SettingsRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CalculatorViewModel(jobId, jobRepository, settingsRepository) as T
            }
        }
    }
}

private fun AppSettingsEntity.toBusinessDetails(): PdfBusinessDetails = PdfBusinessDetails(
    businessName = businessName,
    address = address,
    phoneNumber = businessPhoneDisplay(),
    email = email,
    gstNumber = gstNumber,
    bankAccountNumber = bankAccountNumber,
    bankName = bankName
)
