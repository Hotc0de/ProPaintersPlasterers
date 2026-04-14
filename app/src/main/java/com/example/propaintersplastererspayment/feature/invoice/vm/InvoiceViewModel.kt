package com.example.propaintersplastererspayment.feature.invoice.vm

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.propaintersplastererspayment.core.pdf.InvoiceLinePdfRow
import com.example.propaintersplastererspayment.core.pdf.InvoicePdfData
import com.example.propaintersplastererspayment.core.pdf.PdfBusinessDetails
import com.example.propaintersplastererspayment.core.util.DateFormatUtils
import com.example.propaintersplastererspayment.core.util.InvoiceUtils
import com.example.propaintersplastererspayment.data.local.entity.AppSettingsEntity
import com.example.propaintersplastererspayment.data.local.entity.ClientEntity
import com.example.propaintersplastererspayment.data.local.entity.InvoiceEntity
import com.example.propaintersplastererspayment.data.local.entity.InvoiceLineEntity
import com.example.propaintersplastererspayment.data.local.entity.JobEntity
import com.example.propaintersplastererspayment.data.local.entity.JobStatus
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.util.Locale
import kotlin.math.abs

// ─────────────────────────────────────────────────────────────────────────────
// UI state data classes
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Holds the form values shown in the "Create / Edit Invoice" dialog.
 */
data class InvoiceHeaderFormState(
    val invoiceId: Long? = null,
    val invoiceNumber: TextFieldValue = TextFieldValue(""),
    val billToName: TextFieldValue = TextFieldValue(""),
    val issueDate: TextFieldValue = TextFieldValue(DateFormatUtils.todayDisplayDate()),
    val includeGst: Boolean = true,
    val gstRate: Double = InvoiceUtils.DEFAULT_GST_RATE,
    val notes: TextFieldValue = TextFieldValue(""),
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
    val description: TextFieldValue = TextFieldValue(""),
    val qtyText: TextFieldValue = TextFieldValue("1"),
    val rateText: TextFieldValue = TextFieldValue(""),
    val amountText: TextFieldValue = TextFieldValue(""),
    val isManualAmount: Boolean = false,
    val errorMessage: String? = null
) {
    val parsedQty: Double? get() = qtyText.text.trim().toDoubleOrNull()
    val parsedRate: Double? get() = rateText.text.trim().toDoubleOrNull()
    val parsedAmount: Double? get() = amountText.text.trim().toDoubleOrNull()

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
    val finalTotal: Double = 0.0
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
    val hasImportedLabourLine: Boolean = false,
    val hasImportedMaterialsLine: Boolean = false,
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
    private val selectedBillToClientId = MutableStateFlow<Long?>(null)
    private val pdfExportRequests = MutableSharedFlow<InvoicePdfData>()

    val pdfExportEvents: SharedFlow<InvoicePdfData> = pdfExportRequests.asSharedFlow()

    init {
        observeAndSyncImportedLines()
    }

    /**
     * Reactively observes labour hours, materials, and settings.
     * If corresponding "Labour" or "Materials" lines exist in the current invoice,
     * they are updated automatically to ensure real-time synchronization.
     */
    private fun observeAndSyncImportedLines() {
        viewModelScope.launch {
            invoiceRepository.observeInvoiceForJob(jobId)
                .map { it?.invoiceId }
                .distinctUntilChanged()
                .flatMapLatest { invoiceId ->
                    if (invoiceId == null) return@flatMapLatest flowOf(null)
                    combine(
                        workEntryRepository.observeTotalHoursForJob(jobId),
                        materialRepository.observeTotalMaterialCostForJob(jobId),
                        settingsRepository.observeSettings(),
                        invoiceRepository.observeInvoiceLines(invoiceId),
                        invoiceRepository.observeInvoiceForJob(jobId)
                    ) { hours, matCost, settings, lines, invoice ->
                        if (invoice == null) null
                        else SyncPackage(hours, matCost, settings?.defaultLabourRate ?: 0.0, invoice, lines)
                    }
                }.collect { pkg ->
                    if (pkg != null) {
                        performAutoSync(pkg)
                    }
                }
        }
    }

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

    /** All saved clients for the Bill To dropdown. */
    private val clientSuggestions: StateFlow<List<ClientEntity>> =
        clientRepository.observeClients().stateIn(
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
            hasImportedLabourLine = hasLineWithDescription(core.lines, LABOUR_LINE_DESCRIPTION),
            hasImportedMaterialsLine = hasLineWithDescription(core.lines, MATERIALS_LINE_DESCRIPTION),
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
            val invoiceNumber = invoiceRepository.generateUniqueInvoiceNumber()
            val job = jobRepository.observeJob(jobId).first()
            val clients = clientRepository.observeClients().first()
            val jobClient = job?.clientId?.let { id -> clients.firstOrNull { it.clientId == id } }
            val snapshotClient = if (jobClient == null) {
                clients.firstOrNull {
                    it.name.equals(job?.clientNameSnapshot.orEmpty(), ignoreCase = true)
                }
            } else {
                null
            }
            // Only auto-select the current job client (or exact snapshot match).
            // Do not auto-pick an unrelated first client.
            val selectedClient = jobClient ?: snapshotClient
            val defaultBillTo = selectedClient?.name ?: when {
                job != null -> job.clientNameSnapshot.ifBlank { job.jobName }
                else -> ""
            }
            headerFormState.value = InvoiceHeaderFormState(
                invoiceNumber = TextFieldValue(invoiceNumber, selection = androidx.compose.ui.text.TextRange(invoiceNumber.length)),
                billToName = TextFieldValue(defaultBillTo, selection = androidx.compose.ui.text.TextRange(defaultBillTo.length)),
                includeGst = settings?.gstEnabledByDefault ?: true,
                gstRate = settings?.defaultGstRate ?: InvoiceUtils.DEFAULT_GST_RATE
            )
            selectedBillToClientId.value = selectedClient?.clientId
            isEditingHeader.value = true
        }
    }

    /** Opens the header dialog pre-filled with the existing invoice data. */
    fun openEditHeader(invoice: InvoiceEntity) {
        viewModelScope.launch {
            val clients = clientRepository.observeClients().first()
            val selectedClient = when {
                invoice.clientId != null -> clients.firstOrNull { it.clientId == invoice.clientId }
                else -> clients.firstOrNull {
                    it.name.equals(invoice.billToName, ignoreCase = true)
                }
            }
        val issueDateStr = DateFormatUtils.formatDisplayDate(invoice.issueDate)
        headerFormState.value = InvoiceHeaderFormState(
            invoiceId = invoice.invoiceId,
            invoiceNumber = TextFieldValue(invoice.invoiceNumber, selection = androidx.compose.ui.text.TextRange(invoice.invoiceNumber.length)),
            billToName = TextFieldValue(selectedClient?.name ?: invoice.billToName, selection = androidx.compose.ui.text.TextRange((selectedClient?.name ?: invoice.billToName).length)),
            issueDate = TextFieldValue(issueDateStr, selection = androidx.compose.ui.text.TextRange(issueDateStr.length)),
            includeGst = invoice.includeGst,
            gstRate = invoice.gstRate,
            notes = TextFieldValue(invoice.notes, selection = androidx.compose.ui.text.TextRange(invoice.notes.length))
        )
        selectedBillToClientId.value = selectedClient?.clientId
        isEditingHeader.value = true
        }
    }

    fun dismissHeader() {
        isEditingHeader.value = false
        headerFormState.value = InvoiceHeaderFormState()
        selectedBillToClientId.value = null
    }

    fun onInvoiceNumberChange(value: TextFieldValue) {
        headerFormState.update { it.copy(invoiceNumber = value, errorMessage = null) }
    }

    fun onBillToNameChange(value: TextFieldValue) {
        headerFormState.update { it.copy(billToName = value, errorMessage = null) }
        selectedBillToClientId.value = null
    }

    fun onBillToClientSelected(client: ClientEntity) {
        headerFormState.update { it.copy(billToName = TextFieldValue(client.name, selection = androidx.compose.ui.text.TextRange(client.name.length)), errorMessage = null) }
        selectedBillToClientId.value = client.clientId
    }

    fun onIssueDateChange(value: TextFieldValue) {
        headerFormState.update { it.copy(issueDate = value, errorMessage = null) }
    }

    fun onIncludeGstChange(value: Boolean) {
        headerFormState.update { it.copy(includeGst = value) }
    }

    fun onNotesChange(value: TextFieldValue) {
        headerFormState.update { it.copy(notes = value) }
    }

    /** Validates and saves the invoice header. Shows a snackbar on success. */
    fun saveHeader() {
        val form = headerFormState.value
        val error = InvoiceUtils.validateHeader(
            invoiceNumber = form.invoiceNumber.text,
            billToName = form.billToName.text,
            issueDate = form.issueDate.text
        )
        if (error != null) {
            headerFormState.update { it.copy(errorMessage = error) }
            return
        }

        val storedIssueDate = DateFormatUtils.toStoredDate(form.issueDate.text) ?: run {
            headerFormState.update { it.copy(errorMessage = "Use date format dd-MM-yyyy.") }
            return
        }

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val trimmedBillTo = form.billToName.text.trim()

            val linkedClient = selectedBillToClientId.value?.let { clientRepository.getClient(it) }
                ?: clientRepository.observeClients().first().firstOrNull {
                    it.name.equals(trimmedBillTo, ignoreCase = true)
                }

            val subtotalExclusiveGst = uiState.value.lines.sumOf { it.amount }
            val gstAmount = if (form.includeGst) subtotalExclusiveGst * form.gstRate else 0.0
            val totalAmount = subtotalExclusiveGst + gstAmount
            val existingInvoice = uiState.value.invoice

            invoiceRepository.saveInvoice(
                InvoiceEntity(
                    invoiceId = form.invoiceId ?: 0L,
                    jobId = jobId,
                    clientId = linkedClient?.clientId,
                    invoiceNumber = form.invoiceNumber.text.trim(),
                    invoiceDate = storedIssueDate,
                    billToNameSnapshot = trimmedBillTo.ifBlank { linkedClient?.name.orEmpty() },
                    billToAddressSnapshot = linkedClient?.address.orEmpty(),
                    billToPhoneSnapshot = linkedClient?.phoneNumber.orEmpty(),
                    billToEmailSnapshot = linkedClient?.email.orEmpty(),
                    subtotalExclusiveGst = subtotalExclusiveGst,
                    gstEnabled = form.includeGst,
                    gstRate = form.gstRate,
                    gstAmount = gstAmount,
                    totalAmount = totalAmount,
                    notes = form.notes.text.trim(),
                    createdAt = existingInvoice?.createdAt ?: now,
                    updatedAt = now
                )
            )
            // Update job status to WAITING_FOR_PAYMENT when an invoice is created/updated
            jobRepository.updateJobStatus(jobId, JobStatus.WAITING_FOR_PAYMENT)
            
            userMessage.value = if (form.invoiceId == null) "Invoice created." else "Invoice updated."
            dismissHeader()
        }
    }

    fun markAsPaid() {
        viewModelScope.launch {
            jobRepository.updateJobStatus(jobId, JobStatus.PAID)
            userMessage.value = "Job marked as Paid."
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
            val rateStr = if (defaultRate > 0) defaultRate.toString() else ""
            lineFormState.value = InvoiceLineFormState(
                sortOrder = nextOrder,
                rateText = TextFieldValue(rateStr, selection = androidx.compose.ui.text.TextRange(rateStr.length))
            )
            isEditingLine.value = true
        }
    }

    /** Opens the line dialog pre-filled with an existing line's data. */
    fun openEditLine(line: InvoiceLineEntity) {
        val qtyStr = line.qty.toString()
        val rateStr = line.rate.toString()
        val amountStr = line.amount.toString()
        lineFormState.value = InvoiceLineFormState(
            lineId = line.lineId,
            sortOrder = line.sortOrder,
            description = TextFieldValue(line.description, selection = androidx.compose.ui.text.TextRange(line.description.length)),
            qtyText = TextFieldValue(qtyStr, selection = androidx.compose.ui.text.TextRange(qtyStr.length)),
            rateText = TextFieldValue(rateStr, selection = androidx.compose.ui.text.TextRange(rateStr.length)),
            amountText = TextFieldValue(amountStr, selection = androidx.compose.ui.text.TextRange(amountStr.length)),
            isManualAmount = line.isManualAmount
        )
        isEditingLine.value = true
    }

    fun dismissLine() {
        isEditingLine.value = false
        lineFormState.value = InvoiceLineFormState()
    }

    fun onLineDescriptionChange(value: TextFieldValue) {
        lineFormState.update { it.copy(description = value, errorMessage = null) }
    }

    fun onLineQtyChange(value: TextFieldValue) {
        lineFormState.update { it.copy(qtyText = value, errorMessage = null) }
    }

    fun onLineRateChange(value: TextFieldValue) {
        lineFormState.update { it.copy(rateText = value, errorMessage = null) }
    }

    fun onLineAmountChange(value: TextFieldValue) {
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
            description = form.description.text,
            qtyText = form.qtyText.text,
            rateText = form.rateText.text,
            amountText = form.amountText.text,
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
                    invoiceLineId = form.lineId ?: 0L,
                    invoiceId = invoiceId,
                    description = form.description.text.trim(),
                    qty = qty,
                    rate = rate,
                    amount = effectiveAmount,
                    manualAmountOverride = form.isManualAmount,
                    sortOrder = form.sortOrder
                )
            )
            syncStoredInvoiceTotals(invoiceId)
            userMessage.value = if (form.lineId == null) "Line added." else "Line updated."
            dismissLine()
        }
    }

    /** Deletes a line by its ID. Fetched from DB first to stay consistent with other VMs. */
    fun deleteLine(lineId: Long) {
        viewModelScope.launch {
            val line = invoiceRepository.getInvoiceLine(lineId) ?: return@launch
            val invoiceId = line.invoiceId
            invoiceRepository.deleteInvoiceLine(line)
            syncStoredInvoiceTotals(invoiceId)
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
            val existingLabourLine = uiState.value.lines.firstOrNull {
                it.description.trim().equals(LABOUR_LINE_DESCRIPTION, ignoreCase = true)
            }
            if (existingLabourLine != null) {
                openEditLine(existingLabourLine)
                userMessage.value = "Labour total is already added. You can edit it."
                return@launch
            }

            val totalHours = getTotalLabourHoursForJob()
            val settings = settingsRepository.observeSettings().first()
            val defaultRate = settings?.defaultLabourRate ?: 0.0
            val labourAmount = InvoiceUtils.calculateLabourCost(totalHours, defaultRate)
            val nextOrder = (uiState.value.lines.maxOfOrNull { it.sortOrder } ?: 0) + 1

            val qtyStr = String.format(Locale.US, "%.2f", totalHours)
            val rateStr = if (defaultRate > 0) String.format(Locale.US, "%.2f", defaultRate) else ""
            val amountStr = String.format(Locale.US, "%.2f", labourAmount)

            lineFormState.value = InvoiceLineFormState(
                sortOrder = nextOrder,
                description = TextFieldValue(LABOUR_LINE_DESCRIPTION, selection = androidx.compose.ui.text.TextRange(LABOUR_LINE_DESCRIPTION.length)),
                qtyText = TextFieldValue(qtyStr, selection = androidx.compose.ui.text.TextRange(qtyStr.length)),
                rateText = TextFieldValue(rateStr, selection = androidx.compose.ui.text.TextRange(rateStr.length)),
                amountText = TextFieldValue(amountStr, selection = androidx.compose.ui.text.TextRange(amountStr.length)),
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
            val existingMaterialsLine = uiState.value.lines.firstOrNull {
                it.description.trim().equals(MATERIALS_LINE_DESCRIPTION, ignoreCase = true)
            }
            if (existingMaterialsLine != null) {
                openEditLine(existingMaterialsLine)
                userMessage.value = "Materials total is already added. You can edit it."
                return@launch
            }

            val totalMaterials = getTotalMaterialCostForJob()
            val nextOrder = (uiState.value.lines.maxOfOrNull { it.sortOrder } ?: 0) + 1

            val rateStr = String.format(Locale.US, "%.2f", totalMaterials)
            val amountStr = String.format(Locale.US, "%.2f", totalMaterials)

            lineFormState.value = InvoiceLineFormState(
                sortOrder = nextOrder,
                description = TextFieldValue(MATERIALS_LINE_DESCRIPTION, selection = androidx.compose.ui.text.TextRange(MATERIALS_LINE_DESCRIPTION.length)),
                qtyText = TextFieldValue("1", selection = androidx.compose.ui.text.TextRange(1)),
                rateText = TextFieldValue(rateStr, selection = androidx.compose.ui.text.TextRange(rateStr.length)),
                amountText = TextFieldValue(amountStr, selection = androidx.compose.ui.text.TextRange(amountStr.length)),
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
                issueDate = DateFormatUtils.formatDisplayDate(invoice.issueDate),
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
                totalIncGst = snapshot.totals.finalTotal,
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
        val finalTotal = subtotalExGst + gstAmount

        return InvoiceTotals(
            subtotalExGst = subtotalExGst,
            gstAmount = gstAmount,
            finalTotal = finalTotal
        )
    }

    private fun hasLineWithDescription(lines: List<InvoiceLineEntity>, description: String): Boolean {
        return lines.any { it.description.trim().equals(description, ignoreCase = true) }
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

    /**
     * Automatically updates existing imported lines (Labour/Materials) if the source data
     * has changed. Skips syncing for a line if the user is currently editing it in the dialog.
     */
    private suspend fun performAutoSync(pkg: SyncPackage) {
        var changed = false
        val currentLineEditingId = if (isEditingLine.value) lineFormState.value.lineId else null

        // Sync Labour
        pkg.lines.firstOrNull { it.description.trim().equals(LABOUR_LINE_DESCRIPTION, ignoreCase = true) }?.let { line ->
            // Only sync if not manual override and not currently being edited
            if (!line.manualAmountOverride && line.lineId != currentLineEditingId) {
                val expectedAmount = pkg.totalHours * pkg.labourRate
                if (abs(line.qty - pkg.totalHours) > 0.001 || 
                    abs(line.rate - pkg.labourRate) > 0.001 || 
                    abs(line.amount - expectedAmount) > 0.001) {
                    
                    invoiceRepository.saveInvoiceLine(line.copy(
                        qty = pkg.totalHours,
                        rate = pkg.labourRate,
                        amount = expectedAmount
                    ))
                    changed = true
                }
            }
        }

        // Sync Materials
        pkg.lines.firstOrNull { it.description.trim().equals(MATERIALS_LINE_DESCRIPTION, ignoreCase = true) }?.let { line ->
            // Materials are usually manual override (lump sum), but we still sync the total cost
            if (line.manualAmountOverride && line.lineId != currentLineEditingId) {
                if (abs(line.amount - pkg.totalMaterialCost) > 0.001) {
                    invoiceRepository.saveInvoiceLine(line.copy(
                        amount = pkg.totalMaterialCost,
                        rate = pkg.totalMaterialCost,
                        qty = 1.0
                    ))
                    changed = true
                }
            }
        }

        if (changed) {
            syncStoredInvoiceTotals(pkg.invoice.invoiceId)
        }
    }

    private suspend fun syncStoredInvoiceTotals(invoiceId: Long) {
        val invoice = invoiceRepository.observeInvoiceWithLines(invoiceId).first()?.invoice ?: return
        val lines = invoiceRepository.observeInvoiceLines(invoiceId).first()
        val subtotal = lines.sumOf { it.amount }
        val gstAmount = if (invoice.gstEnabled) subtotal * invoice.gstRate else 0.0
        val total = subtotal + gstAmount

        // Only update if numerical values changed to avoid infinite loop via updatedAt
        if (abs(invoice.subtotalExclusiveGst - subtotal) > 0.001 ||
            abs(invoice.gstAmount - gstAmount) > 0.001 ||
            abs(invoice.totalAmount - total) > 0.001) {
            
            invoiceRepository.saveInvoice(
                invoice.copy(
                    subtotalExclusiveGst = subtotal,
                    gstAmount = gstAmount,
                    totalAmount = total,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
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

    private data class SyncPackage(
        val totalHours: Double,
        val totalMaterialCost: Double,
        val labourRate: Double,
        val invoice: InvoiceEntity,
        val lines: List<InvoiceLineEntity>
    )

    // ─────────────────────────────────────────────────────────────────────
    // Factory
    // ─────────────────────────────────────────────────────────────────────

    companion object {
        private const val LABOUR_LINE_DESCRIPTION = "Labour"
        private const val MATERIALS_LINE_DESCRIPTION = "Materials"

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



