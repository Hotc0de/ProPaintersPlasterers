package com.example.propaintersplastererspayment.feature.invoice.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.propaintersplastererspayment.core.pdf.InvoiceLinePdfRow
import com.example.propaintersplastererspayment.core.pdf.InvoicePdfData
import com.example.propaintersplastererspayment.core.pdf.PdfBusinessDetails
import com.example.propaintersplastererspayment.core.util.InvoiceUtils
import com.example.propaintersplastererspayment.data.local.entity.AppSettingsEntity
import com.example.propaintersplastererspayment.data.local.entity.ClientEntity
import com.example.propaintersplastererspayment.data.local.entity.InvoiceEntity
import com.example.propaintersplastererspayment.data.local.entity.InvoiceLineEntity
import com.example.propaintersplastererspayment.data.local.entity.JobEntity
import com.example.propaintersplastererspayment.domain.repository.ClientRepository
import com.example.propaintersplastererspayment.domain.repository.InvoiceRepository
import com.example.propaintersplastererspayment.domain.repository.JobRepository
import com.example.propaintersplastererspayment.domain.repository.MaterialRepository
import com.example.propaintersplastererspayment.domain.repository.SettingsRepository
import com.example.propaintersplastererspayment.domain.repository.WorkEntryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// UI state data classes
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Holds the form values shown in the "Create / Edit Invoice" dialog.
 *
 * The [otherAmountText] field lets the user enter extra amounts (e.g. delivery,
 * discount) that are added to the subtotal after GST.
 */
data class InvoiceHeaderFormState(
    val invoiceId: Long? = null,
    val invoiceNumber: String = "",
    val billToName: String = "",
    val issueDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val includeGst: Boolean = true,
    val gstRate: Double = InvoiceUtils.DEFAULT_GST_RATE,
    val otherAmountText: String = "0",
    val notes: String = "",
    val errorMessage: String? = null
)

/**
 * Holds the form values for adding or editing a single invoice line.
 *
 * When [isManualAmount] is false the [effectiveAmount] is calculated as qty × rate.
 * When [isManualAmount] is true the user directly enters the [amountText] — useful
 * for inserting a flat material total from the Timesheet feature.
 */
data class InvoiceLineFormState(
    val lineId: Long? = null,
    val sortOrder: Int = 0,
    val description: String = "",
    val qtyText: String = "1",
    val rateText: String = "",
    val amountText: String = "",
    val isManualAmount: Boolean = false,
    val errorMessage: String? = null
) {
    val parsedQty: Double? get() = qtyText.trim().toDoubleOrNull()
    val parsedRate: Double? get() = rateText.trim().toDoubleOrNull()
    val parsedAmount: Double? get() = amountText.trim().toDoubleOrNull()

    /**
     * The amount that will be saved.
     * - If manual: whatever the user typed in [amountText].
     * - Otherwise: qty × rate (or null if either field is not valid yet).
     */
    val effectiveAmount: Double?
        get() = if (isManualAmount) {
            parsedAmount
        } else {
            val q = parsedQty
            val r = parsedRate
            if (q != null && r != null) q * r else null
        }
}

/**
 * Pre-calculated totals that are derived from the invoice + its lines.
 * These are recomputed automatically every time the invoice or lines change.
 */
data class InvoiceTotals(
    val subtotalExGst: Double = 0.0,
    val gstAmount: Double = 0.0,
    val subtotalIncGst: Double = 0.0, // subtotal + GST
    val otherAmount: Double = 0.0,
    val finalTotal: Double = 0.0      // subtotalIncGst + otherAmount
)

/**
 * The single source of truth for the InvoiceScreen.
 */
data class InvoiceUiState(
    val job: JobEntity? = null,
    val invoice: InvoiceEntity? = null,
    val lines: List<InvoiceLineEntity> = emptyList(),
    val totals: InvoiceTotals = InvoiceTotals(),
    val isLoading: Boolean = true,
    // Header dialog
    val isEditingHeader: Boolean = false,
    val headerFormState: InvoiceHeaderFormState = InvoiceHeaderFormState(),
    // Line dialog
    val isEditingLine: Boolean = false,
    val lineFormState: InvoiceLineFormState = InvoiceLineFormState(),
    // Client auto-suggestions while the user types in the Bill To field
    val clientSuggestions: List<ClientEntity> = emptyList(),
    val userMessage: String? = null
)

// ─────────────────────────────────────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Manages all state for the Invoice screen.
 *
 * Responsibilities:
 *  - Load the job details and the invoice (or null if not yet created).
 *  - Observe invoice lines reactively via flatMapLatest (lines depend on invoiceId).
 *  - Provide client name suggestions while the user types in the Bill To field.
 *  - Handle create / edit / delete for the invoice header and individual lines.
 *  - Pre-fill "Import Labour" and "Import Materials" lines from existing job data.
 *  - Compute invoice totals (GST, subtotal, final) reactively.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class InvoiceViewModel(
    private val jobId: Long,
    private val jobRepository: JobRepository,
    private val invoiceRepository: InvoiceRepository,
    private val clientRepository: ClientRepository,
    private val settingsRepository: SettingsRepository,
    private val materialRepository: MaterialRepository,
    private val workEntryRepository: WorkEntryRepository
) : ViewModel() {

    // ── Form-related mutable state ─────────────────────────────────────────

    private val isEditingHeader = MutableStateFlow(false)
    private val headerFormState = MutableStateFlow(InvoiceHeaderFormState())
    private val isEditingLine = MutableStateFlow(false)
    private val lineFormState = MutableStateFlow(InvoiceLineFormState())
    private val userMessage = MutableStateFlow<String?>(null)
    private val pdfExportRequests = MutableSharedFlow<InvoicePdfData>()

    val pdfExportEvents: SharedFlow<InvoicePdfData> = pdfExportRequests.asSharedFlow()

    /**
     * The text the user has typed in the Bill To field — used to drive client suggestions.
     * Separate from [headerFormState.billToName] so that we can clear suggestions without
     * clearing the field itself when the user picks a suggestion.
     */
    private val billToSearchQuery = MutableStateFlow("")

    // ── Database streams ───────────────────────────────────────────────────

    /**
     * The invoice lines for the current job.
     * Uses flatMapLatest so it automatically re-subscribes when the invoice ID changes
     * (e.g. after the invoice is first created).
     */
    private val linesFlow: Flow<List<InvoiceLineEntity>> =
        invoiceRepository.observeInvoiceForJob(jobId).flatMapLatest { invoice ->
            if (invoice != null) {
                invoiceRepository.observeInvoiceLines(invoice.invoiceId)
            } else {
                flowOf(emptyList())
            }
        }

    /**
     * Combines job + invoice + lines into one object so they can be used together
     * in the outer [uiState] combine without hitting the 5-flow limit.
     */
    private val coreData = combine(
        jobRepository.observeJob(jobId),
        invoiceRepository.observeInvoiceForJob(jobId),
        linesFlow
    ) { job, invoice, lines -> CoreData(job, invoice, lines) }

    /**
     * Client suggestions driven by what the user types in the Bill To field.
     * Returns an empty list when the query is blank (avoids loading all clients at once).
     */
    private val clientSuggestions: StateFlow<List<ClientEntity>> =
        billToSearchQuery
            .flatMapLatest { query ->
                if (query.isNotEmpty()) {
                    clientRepository.observeSuggestions(query)
                } else {
                    flowOf(emptyList())
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    /**
     * Bundles all form-related flows into one object for use in the outer combine.
     */
    private val formData = combine(
        isEditingHeader,
        headerFormState,
        isEditingLine,
        lineFormState,
        userMessage
    ) { editHeader, hForm, editLine, lForm, msg ->
        FormData(editHeader, hForm, editLine, lForm, msg)
    }

    // ── Public UI state ────────────────────────────────────────────────────

    val uiState: StateFlow<InvoiceUiState> = combine(
        coreData,
        formData,
        clientSuggestions
    ) { core, forms, suggestions ->
        InvoiceUiState(
            job = core.job,
            invoice = core.invoice,
            lines = core.lines,
            totals = computeTotals(core.invoice, core.lines),
            isLoading = false,
            isEditingHeader = forms.isEditingHeader,
            headerFormState = forms.headerFormState,
            isEditingLine = forms.isEditingLine,
            lineFormState = forms.lineFormState,
            clientSuggestions = suggestions,
            userMessage = forms.userMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = InvoiceUiState()
    )

    // ─────────────────────────────────────────────────────────────────────
    // Header form actions
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Opens the header dialog pre-filled with a generated invoice number and the
     * GST default from Settings.
     */
    fun openCreateInvoice() {
        viewModelScope.launch {
            val settings = settingsRepository.observeSettings().first()
            val prefix = settings?.invoiceNumberPrefix?.ifBlank { "INV-" } ?: "INV-"
            val invoiceNumber = invoiceRepository.getNextInvoiceNumber(prefix)
            headerFormState.value = InvoiceHeaderFormState(
                invoiceNumber = invoiceNumber,
                includeGst = settings?.gstEnabledByDefault ?: true,
                gstRate = settings?.defaultGstRate ?: InvoiceUtils.DEFAULT_GST_RATE
            )
            isEditingHeader.value = true
        }
    }

    /** Opens the header dialog pre-filled with the existing invoice data. */
    fun openEditHeader(invoice: InvoiceEntity) {
        headerFormState.value = InvoiceHeaderFormState(
            invoiceId = invoice.invoiceId,
            invoiceNumber = invoice.invoiceNumber,
            billToName = invoice.billToName,
            issueDate = invoice.issueDate,
            includeGst = invoice.includeGst,
            gstRate = invoice.gstRate,
            otherAmountText = if (invoice.otherAmount == 0.0) "0" else invoice.otherAmount.toString(),
            notes = invoice.notes
        )
        isEditingHeader.value = true
    }

    fun dismissHeader() {
        isEditingHeader.value = false
        headerFormState.value = InvoiceHeaderFormState()
        billToSearchQuery.value = ""
    }

    fun onInvoiceNumberChange(value: String) {
        headerFormState.update { it.copy(invoiceNumber = value, errorMessage = null) }
    }

    /**
     * Called on every keystroke in the Bill To field.
     * Updates both the form state and the suggestion query.
     */
    fun onBillToNameChange(value: String) {
        headerFormState.update { it.copy(billToName = value, errorMessage = null) }
        billToSearchQuery.value = value
    }

    /**
     * Called when the user taps a client suggestion chip.
     * Fills the Bill To field and clears the suggestion list.
     */
    fun onBillToClientSelected(client: ClientEntity) {
        headerFormState.update { it.copy(billToName = client.name, errorMessage = null) }
        billToSearchQuery.value = "" // clears suggestions
    }

    fun onIssueDateChange(value: String) {
        headerFormState.update { it.copy(issueDate = value, errorMessage = null) }
    }

    fun onIncludeGstChange(value: Boolean) {
        headerFormState.update { it.copy(includeGst = value) }
    }

    fun onOtherAmountChange(value: String) {
        headerFormState.update { it.copy(otherAmountText = value, errorMessage = null) }
    }

    fun onNotesChange(value: String) {
        headerFormState.update { it.copy(notes = value) }
    }

    /** Validates and saves the invoice header. Shows a snackbar on success. */
    fun saveHeader() {
        val form = headerFormState.value
        val error = InvoiceUtils.validateHeader(
            invoiceNumber = form.invoiceNumber,
            billToName = form.billToName,
            issueDate = form.issueDate,
            otherAmountText = form.otherAmountText
        )
        if (error != null) {
            headerFormState.update { it.copy(errorMessage = error) }
            return
        }

        val otherAmount = InvoiceUtils.parseAmount(form.otherAmountText) ?: 0.0

        viewModelScope.launch {
            if (form.billToName.isNotBlank()) {
                clientRepository.saveClient(
                    ClientEntity(name = form.billToName.trim())
                )
            }

            invoiceRepository.saveInvoice(
                InvoiceEntity(
                    invoiceId = form.invoiceId ?: 0L,
                    jobOwnerId = jobId,
                    invoiceNumber = form.invoiceNumber.trim(),
                    billToName = form.billToName.trim(),
                    issueDate = form.issueDate.trim(),
                    includeGst = form.includeGst,
                    gstRate = form.gstRate,
                    otherAmount = otherAmount,
                    notes = form.notes.trim()
                )
            )
            userMessage.value = if (form.invoiceId == null) "Invoice created." else "Invoice updated."
            dismissHeader()
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Line form actions
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Opens the line dialog for a new blank line.
     * Pre-fills the default labour rate from Settings (handy for labour lines).
     */
    fun openAddLine() {
        viewModelScope.launch {
            val settings = settingsRepository.observeSettings().first()
            val defaultRate = settings?.defaultLabourRate ?: 0.0
            val nextOrder = (uiState.value.lines.maxOfOrNull { it.sortOrder } ?: 0) + 1
            lineFormState.value = InvoiceLineFormState(
                sortOrder = nextOrder,
                rateText = if (defaultRate > 0) defaultRate.toString() else ""
            )
            isEditingLine.value = true
        }
    }

    /** Opens the line dialog pre-filled with an existing line's data. */
    fun openEditLine(line: InvoiceLineEntity) {
        lineFormState.value = InvoiceLineFormState(
            lineId = line.lineId,
            sortOrder = line.sortOrder,
            description = line.description,
            qtyText = line.qty.toString(),
            rateText = line.rate.toString(),
            amountText = line.amount.toString(),
            isManualAmount = line.isManualAmount
        )
        isEditingLine.value = true
    }

    fun dismissLine() {
        isEditingLine.value = false
        lineFormState.value = InvoiceLineFormState()
    }

    fun onLineDescriptionChange(value: String) {
        lineFormState.update { it.copy(description = value, errorMessage = null) }
    }

    fun onLineQtyChange(value: String) {
        lineFormState.update { it.copy(qtyText = value, errorMessage = null) }
    }

    fun onLineRateChange(value: String) {
        lineFormState.update { it.copy(rateText = value, errorMessage = null) }
    }

    fun onLineAmountChange(value: String) {
        lineFormState.update { it.copy(amountText = value, errorMessage = null) }
    }

    fun onLineManualAmountChange(value: Boolean) {
        lineFormState.update { it.copy(isManualAmount = value) }
    }

    /** Validates and saves the invoice line. Shows a snackbar on success. */
    fun saveLine() {
        val form = lineFormState.value
        // Can't save a line without an invoice to attach it to
        val invoiceId = uiState.value.invoice?.invoiceId ?: return

        val error = InvoiceUtils.validateLine(
            description = form.description,
            qtyText = form.qtyText,
            rateText = form.rateText,
            amountText = form.amountText,
            isManualAmount = form.isManualAmount
        )
        if (error != null) {
            lineFormState.update { it.copy(errorMessage = error) }
            return
        }

        val effectiveAmount = form.effectiveAmount ?: return
        val qty = form.parsedQty ?: 1.0
        val rate = form.parsedRate ?: 0.0

        viewModelScope.launch {
            invoiceRepository.saveInvoiceLine(
                InvoiceLineEntity(
                    lineId = form.lineId ?: 0L,
                    invoiceOwnerId = invoiceId,
                    description = form.description.trim(),
                    qty = qty,
                    rate = rate,
                    amount = effectiveAmount,
                    isManualAmount = form.isManualAmount,
                    sortOrder = form.sortOrder
                )
            )
            userMessage.value = if (form.lineId == null) "Line added." else "Line updated."
            dismissLine()
        }
    }

    /** Deletes a line by its ID. Fetched from DB first to stay consistent with other VMs. */
    fun deleteLine(lineId: Long) {
        viewModelScope.launch {
            val line = invoiceRepository.getInvoiceLine(lineId) ?: return@launch
            invoiceRepository.deleteInvoiceLine(line)
            userMessage.value = "Line deleted."
            if (lineFormState.value.lineId == lineId) {
                dismissLine()
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Import helpers — pull data from the Timesheet / Materials tabs
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Pre-fills the line dialog with total hours × default labour rate.
     * The user can still edit both before saving.
     */
    fun openAddLabourLine() {
        viewModelScope.launch {
            val totalHours = getTotalLabourHoursForJob()
            val settings = settingsRepository.observeSettings().first()
            val defaultRate = settings?.defaultLabourRate ?: 0.0
            val labourAmount = InvoiceUtils.calculateLabourCost(totalHours, defaultRate)
            val nextOrder = (uiState.value.lines.maxOfOrNull { it.sortOrder } ?: 0) + 1

            lineFormState.value = InvoiceLineFormState(
                sortOrder = nextOrder,
                description = "Labour",
                qtyText = String.format(Locale.US, "%.2f", totalHours),
                rateText = if (defaultRate > 0) String.format(Locale.US, "%.2f", defaultRate) else "",
                amountText = String.format(Locale.US, "%.2f", labourAmount),
                isManualAmount = false
            )
            isEditingLine.value = true
        }
    }

    /**
     * Pre-fills the line dialog with the total materials cost as a flat amount.
     * Sets [isManualAmount] = true because it's a single lump-sum rather than qty × rate.
     */
    fun openAddMaterialsLine() {
        viewModelScope.launch {
            val totalMaterials = getTotalMaterialCostForJob()
            val nextOrder = (uiState.value.lines.maxOfOrNull { it.sortOrder } ?: 0) + 1

            lineFormState.value = InvoiceLineFormState(
                sortOrder = nextOrder,
                description = "Materials",
                qtyText = "1",
                rateText = String.format(Locale.US, "%.2f", totalMaterials),
                amountText = String.format(Locale.US, "%.2f", totalMaterials),
                isManualAmount = true
            )
            isEditingLine.value = true
        }
    }

    fun clearUserMessage() {
        userMessage.value = null
    }

    fun exportInvoicePdf() {
        viewModelScope.launch {
            val snapshot = uiState.value
            val job = snapshot.job
            val invoice = snapshot.invoice
            if (job == null || invoice == null) {
                userMessage.value = "Create an invoice before exporting PDF."
                return@launch
            }

            val settings = settingsRepository.observeSettings().first() ?: AppSettingsEntity()

            val data = InvoicePdfData(
                fileName = "invoice-${invoice.invoiceNumber}.pdf",
                exportedAt = InvoiceUtils.todayDate(),
                business = settings.toBusinessDetails(),
                jobName = job.jobName,
                jobAddress = job.propertyAddress,
                invoiceNumber = invoice.invoiceNumber,
                issueDate = invoice.issueDate,
                billToName = invoice.billToName,
                lines = snapshot.lines.map { line ->
                    InvoiceLinePdfRow(
                        description = line.description,
                        qty = line.qty,
                        rate = line.rate,
                        amount = line.amount,
                        isManualAmount = line.isManualAmount
                    )
                },
                subtotalExGst = snapshot.totals.subtotalExGst,
                includeGst = invoice.includeGst,
                gstRate = invoice.gstRate,
                gstAmount = snapshot.totals.gstAmount,
                totalIncGst = snapshot.totals.subtotalIncGst,
                otherAmount = snapshot.totals.otherAmount,
                finalTotal = snapshot.totals.finalTotal,
                notes = invoice.notes
            )

            pdfExportRequests.emit(data)
        }
    }

    fun onPdfExportFinished(success: Boolean) {
        userMessage.value = if (success) {
            "Invoice PDF exported."
        } else {
            "Failed to export invoice PDF."
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Derives [InvoiceTotals] from the current invoice settings and its lines.
     * Called inside the reactive combine so totals always stay in sync.
     */
    private fun computeTotals(
        invoice: InvoiceEntity?,
        lines: List<InvoiceLineEntity>
    ): InvoiceTotals {
        if (invoice == null) return InvoiceTotals()

        val subtotalExGst = lines.sumOf { it.amount }
        val gstAmount = if (invoice.includeGst) subtotalExGst * invoice.gstRate else 0.0
        val subtotalIncGst = subtotalExGst + gstAmount
        val otherAmount = invoice.otherAmount
        val finalTotal = subtotalIncGst + otherAmount

        return InvoiceTotals(
            subtotalExGst = subtotalExGst,
            gstAmount = gstAmount,
            subtotalIncGst = subtotalIncGst,
            otherAmount = otherAmount,
            finalTotal = finalTotal
        )
    }

    /** Reads and calculates total hours from all timesheet entries for this job. */
    private suspend fun getTotalLabourHoursForJob(): Double {
        val entries = workEntryRepository.observeEntriesForJob(jobId).first()
        return InvoiceUtils.calculateTotalLabourHours(entries.map { it.hoursWorked })
    }

    /** Reads and calculates total material cost for this job. */
    private suspend fun getTotalMaterialCostForJob(): Double {
        val materials = materialRepository.observeMaterialsForJob(jobId).first()
        return InvoiceUtils.calculateTotalMaterialCost(materials.map { it.price })
    }

    // ─────────────────────────────────────────────────────────────────────
    // Private data classes (only needed inside the ViewModel)
    // ─────────────────────────────────────────────────────────────────────

    private data class CoreData(
        val job: JobEntity?,
        val invoice: InvoiceEntity?,
        val lines: List<InvoiceLineEntity>
    )

    private data class FormData(
        val isEditingHeader: Boolean,
        val headerFormState: InvoiceHeaderFormState,
        val isEditingLine: Boolean,
        val lineFormState: InvoiceLineFormState,
        val userMessage: String?
    )

    // ─────────────────────────────────────────────────────────────────────
    // Factory
    // ─────────────────────────────────────────────────────────────────────

    companion object {
        /**
         * Creates a [ViewModelProvider.Factory] that passes all dependencies into the
         * ViewModel constructor. Called from [InvoiceRoute] using the app container.
         */
        fun provideFactory(
            jobId: Long,
            jobRepository: JobRepository,
            invoiceRepository: InvoiceRepository,
            clientRepository: ClientRepository,
            settingsRepository: SettingsRepository,
            materialRepository: MaterialRepository,
            workEntryRepository: WorkEntryRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return InvoiceViewModel(
                    jobId,
                    jobRepository,
                    invoiceRepository,
                    clientRepository,
                    settingsRepository,
                    materialRepository,
                    workEntryRepository
                ) as T
            }
        }
    }
}

private fun AppSettingsEntity.toBusinessDetails(): PdfBusinessDetails = PdfBusinessDetails(
    businessName = businessName,
    address = address,
    phoneNumber = phoneNumber,
    email = email,
    gstNumber = gstNumber,
    bankAccountNumber = bankAccountNumber
)



