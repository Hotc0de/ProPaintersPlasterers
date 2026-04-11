package com.example.propaintersplastererspayment.feature.invoice.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.propaintersplastererspayment.ProPaintersApplication
import com.example.propaintersplastererspayment.R
import com.example.propaintersplastererspayment.core.ui.MinActionButtonWidth
import com.example.propaintersplastererspayment.core.ui.isCompactActionRowWidth
import com.example.propaintersplastererspayment.core.ui.isCompactPhoneWidth
import com.example.propaintersplastererspayment.core.pdf.PdfExportService
import com.example.propaintersplastererspayment.core.pdf.PdfFileHelper
import com.example.propaintersplastererspayment.core.util.CurrencyFormatUtils
import com.example.propaintersplastererspayment.core.util.DateFormatUtils
import com.example.propaintersplastererspayment.core.util.InvoiceUtils
import com.example.propaintersplastererspayment.data.local.entity.ClientEntity
import com.example.propaintersplastererspayment.data.local.entity.InvoiceEntity
import com.example.propaintersplastererspayment.data.local.entity.InvoiceLineEntity
import com.example.propaintersplastererspayment.data.local.entity.JobEntity
import com.example.propaintersplastererspayment.feature.invoice.vm.InvoiceHeaderFormState
import com.example.propaintersplastererspayment.feature.invoice.vm.InvoiceLineFormState
import com.example.propaintersplastererspayment.feature.invoice.vm.InvoiceTotals
import com.example.propaintersplastererspayment.feature.invoice.vm.InvoiceUiState
import com.example.propaintersplastererspayment.feature.invoice.vm.InvoiceViewModel
import androidx.compose.ui.graphics.Color
import com.example.propaintersplastererspayment.ui.theme.OxfordBlue
import com.example.propaintersplastererspayment.ui.theme.GoldAccent
import com.example.propaintersplastererspayment.ui.theme.ProPaintersPlasterersPaymentTheme

// ─────────────────────────────────────────────────────────────────────────────
// Route — wires the ViewModel to the screen
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Entry point composable for the Invoice feature.
 * Resolves the [InvoiceViewModel] from the app container and passes all actions
 * down to the stateless [InvoiceScreen].
 */
@Composable
fun InvoiceRoute(
    jobId: Long,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val application = context.applicationContext as ProPaintersApplication
    val pdfExportService = remember { PdfExportService() }
    val viewModel: InvoiceViewModel = viewModel(
        factory = InvoiceViewModel.provideFactory(
            jobId = jobId,
            jobRepository = application.container.jobRepository,
            invoiceRepository = application.container.invoiceRepository,
            clientRepository = application.container.clientRepository,
            settingsRepository = application.container.settingsRepository,
            materialRepository = application.container.materialRepository,
            workEntryRepository = application.container.workEntryRepository
        )
    )
    val uiState by viewModel.uiState.collectAsState()
    // Capture string resource at composable scope so it's safe to use inside coroutines
    val pdfShareTitle = stringResource(R.string.pdf_share_invoice)

    LaunchedEffect(viewModel) {
        viewModel.pdfExportEvents.collect { exportData ->
            runCatching {
                val outputFile = PdfFileHelper.createExportFile(context, exportData.fileName)
                pdfExportService.exportInvoicePdf(context, exportData, outputFile)
                PdfFileHelper.sharePdf(
                    context = context,
                    file = outputFile,
                    chooserTitle = pdfShareTitle
                )
            }.onSuccess {
                viewModel.onPdfExportFinished(success = true)
            }.onFailure {
                viewModel.onPdfExportFinished(success = false)
            }
        }
    }

    InvoiceScreen(
        uiState = uiState,
        modifier = modifier,
        onExportPdf = viewModel::exportInvoicePdf,
        // Header
        onCreateInvoice = viewModel::openCreateInvoice,
        onEditHeader = viewModel::openEditHeader,
        onDismissHeader = viewModel::dismissHeader,
        onInvoiceNumberChange = viewModel::onInvoiceNumberChange,
        onBillToClientSelected = viewModel::onBillToClientSelected,
        onIssueDateChange = viewModel::onIssueDateChange,
        onIncludeGstChange = viewModel::onIncludeGstChange,
        onOtherAmountChange = viewModel::onOtherAmountChange,
        onNotesChange = viewModel::onNotesChange,
        onSaveHeader = viewModel::saveHeader,
        // Lines
        onAddLine = viewModel::openAddLine,
        onEditLine = viewModel::openEditLine,
        onDismissLine = viewModel::dismissLine,
        onLineDescriptionChange = viewModel::onLineDescriptionChange,
        onLineQtyChange = viewModel::onLineQtyChange,
        onLineRateChange = viewModel::onLineRateChange,
        onLineAmountChange = viewModel::onLineAmountChange,
        onLineManualAmountChange = viewModel::onLineManualAmountChange,
        onSaveLine = viewModel::saveLine,
        onDeleteLine = viewModel::deleteLine,
        // Import helpers
        onAddLabourLine = viewModel::openAddLabourLine,
        onAddMaterialsLine = viewModel::openAddMaterialsLine,
        onMessageShown = viewModel::clearUserMessage
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Main stateless screen
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Stateless invoice screen. All state comes from [uiState]; actions are lambdas.
 *
 * Layout:
 *  1. Job summary card
 *  2. Invoice header card (or "Create Invoice" call-to-action)
 *  3. Import shortcut buttons (only when an invoice exists)
 *  4. List of invoice lines
 *  5. Totals card
 *
 * Dialogs overlay the screen when the header or line editing flags are true.
 */
@Composable
fun InvoiceScreen(
    uiState: InvoiceUiState,
    onExportPdf: () -> Unit,
    onCreateInvoice: () -> Unit,
    onEditHeader: (InvoiceEntity) -> Unit,
    onDismissHeader: () -> Unit,
    onInvoiceNumberChange: (String) -> Unit,
    onBillToClientSelected: (ClientEntity) -> Unit,
    onIssueDateChange: (String) -> Unit,
    onIncludeGstChange: (Boolean) -> Unit,
    onOtherAmountChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSaveHeader: () -> Unit,
    onAddLine: () -> Unit,
    onEditLine: (InvoiceLineEntity) -> Unit,
    onDismissLine: () -> Unit,
    onLineDescriptionChange: (String) -> Unit,
    onLineQtyChange: (String) -> Unit,
    onLineRateChange: (String) -> Unit,
    onLineAmountChange: (String) -> Unit,
    onLineManualAmountChange: (Boolean) -> Unit,
    onSaveLine: () -> Unit,
    onDeleteLine: (Long) -> Unit,
    onAddLabourLine: () -> Unit,
    onAddMaterialsLine: () -> Unit,
    onMessageShown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Show a snackbar for each user-facing message, then clear it
    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onMessageShown()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            // FAB only appears once an invoice has been created
            if (uiState.invoice != null) {
                ExtendedFloatingActionButton(
                    onClick = onAddLine,
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(text = stringResource(R.string.invoice_add_line))
                }
            }
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.job == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.invoice_no_job),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // ── Job summary ────────────────────────────────────────
                    item {
                        InvoiceJobSummaryCard(job = uiState.job)
                    }

                    // ── Invoice header (or CTA) ────────────────────────────
                    item {
                        if (uiState.invoice == null) {
                            NoInvoiceCard(onCreateInvoice = onCreateInvoice)
                        } else {
                            InvoiceHeaderCard(
                                invoice = uiState.invoice,
                                onExportPdf = onExportPdf,
                                onEdit = { onEditHeader(uiState.invoice) }
                            )
                        }
                    }

                    // ── Import shortcuts + lines (only when invoice exists) ─
                    if (uiState.invoice != null) {
                        item {
                            ImportActionsRow(
                                onAddLabourLine = onAddLabourLine,
                                onAddMaterialsLine = onAddMaterialsLine,
                                labourAlreadyAdded = uiState.hasImportedLabourLine,
                                materialsAlreadyAdded = uiState.hasImportedMaterialsLine
                            )
                        }

                        item {
                            Text(
                                text = stringResource(R.string.invoice_lines_header),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }

                        if (uiState.lines.isEmpty()) {
                            item { EmptyInvoiceLinesCard() }
                        } else {
                            items(uiState.lines, key = { it.lineId }) { line ->
                                InvoiceLineCard(
                                    line = line,
                                    onEdit = { onEditLine(line) }
                                )
                            }
                        }

                        if (uiState.totals.otherAmount != 0.0) {
                            item {
                                InvoiceOtherAmountLineCard(otherAmount = uiState.totals.otherAmount)
                            }
                        }

                        // ── Totals ─────────────────────────────────────────
                        item {
                            InvoiceTotalsCard(
                                invoice = uiState.invoice,
                                totals = uiState.totals
                            )
                        }

                        // Bottom padding so the FAB doesn't cover the last item
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }

    // ── Dialogs ────────────────────────────────────────────────────────────
    if (uiState.isEditingHeader) {
        InvoiceHeaderDialog(
            formState = uiState.headerFormState,
            clientSuggestions = uiState.clientSuggestions,
            onDismiss = onDismissHeader,
            onInvoiceNumberChange = onInvoiceNumberChange,
            onBillToClientSelected = onBillToClientSelected,
            onIssueDateChange = onIssueDateChange,
            onIncludeGstChange = onIncludeGstChange,
            onSave = onSaveHeader
        )
    }

    if (uiState.isEditingLine) {
        InvoiceLineDialog(
            formState = uiState.lineFormState,
            onDismiss = onDismissLine,
            onDescriptionChange = onLineDescriptionChange,
            onQtyChange = onLineQtyChange,
            onRateChange = onLineRateChange,
            onAmountChange = onLineAmountChange,
            onManualAmountChange = onLineManualAmountChange,
            onSave = onSaveLine,
            onDelete = {
                uiState.lineFormState.lineId?.let(onDeleteLine)
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sub-composables
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun InvoiceJobSummaryCard(job: JobEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = OxfordBlue),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Box {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .matchParentSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                GoldAccent,
                                GoldAccent.copy(alpha = 0.7f)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier.padding(start = 22.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = job.clientName.ifBlank {
                        job.jobName.ifBlank { stringResource(R.string.invoice_unknown_job) }
                    },
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${stringResource(R.string.invoice_job_address)}: ${job.propertyAddress}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** Shown when no invoice has been created for this job yet. */
@Composable
private fun NoInvoiceCard(onCreateInvoice: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.invoice_no_invoice),
                style = MaterialTheme.typography.bodyLarge
            )
            OutlinedButton(onClick = onCreateInvoice) {
                Text(text = stringResource(R.string.invoice_create))
            }
        }
    }
}

/** Shows the saved invoice header details with an Edit button. */
@Composable
private fun InvoiceHeaderCard(
    invoice: InvoiceEntity,
    onExportPdf: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = OxfordBlue),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        BoxWithConstraints {
            val compactLayout = isCompactPhoneWidth(maxWidth)
            val compactActionLayout = isCompactActionRowWidth(maxWidth)

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (compactActionLayout) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = invoice.invoiceNumber,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = onExportPdf,
                                modifier = Modifier.widthIn(min = 132.dp),
                                colors = ButtonDefaults.textButtonColors(contentColor = GoldAccent)
                            ) {
                                Text(
                                    text = stringResource(R.string.pdf_export_invoice),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = invoice.invoiceNumber,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = onExportPdf,
                            modifier = Modifier.widthIn(min = 132.dp),
                            colors = ButtonDefaults.textButtonColors(contentColor = GoldAccent)
                        ) {
                            Text(
                                text = stringResource(R.string.pdf_export_invoice),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                LabelValue(
                    label = stringResource(R.string.invoice_bill_to),
                    value = invoice.billToName,
                    stacked = compactLayout,
                    color = Color.White
                )
                LabelValue(
                    label = stringResource(R.string.invoice_date),
                    value = DateFormatUtils.formatDisplayDate(invoice.issueDate),
                    stacked = compactLayout,
                    color = Color.White
                )
                Text(
                    text = if (invoice.includeGst) {
                        stringResource(R.string.invoice_include_gst)
                    } else {
                        stringResource(R.string.invoice_no_gst)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (invoice.notes.isNotBlank()) {
                    Text(
                        text = invoice.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f),
                        maxLines = if (compactLayout) 3 else 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onEdit,
                        modifier = Modifier.widthIn(min = MinActionButtonWidth),
                        colors = ButtonDefaults.textButtonColors(contentColor = GoldAccent)
                    ) {
                        Text(
                            text = stringResource(R.string.invoice_edit),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/** Two small buttons to quickly import labour or materials totals as a line. */
@Composable
private fun ImportActionsRow(
    onAddLabourLine: () -> Unit,
    onAddMaterialsLine: () -> Unit,
    labourAlreadyAdded: Boolean,
    materialsAlreadyAdded: Boolean
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        val compactLayout = isCompactPhoneWidth(maxWidth)
        val compactActionLayout = isCompactActionRowWidth(maxWidth)

        if (compactLayout || compactActionLayout) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onAddLabourLine,
                    enabled = !labourAlreadyAdded,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.invoice_add_labour_line),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                OutlinedButton(
                    onClick = onAddMaterialsLine,
                    enabled = !materialsAlreadyAdded,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.invoice_add_materials_line),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onAddLabourLine,
                    enabled = !labourAlreadyAdded,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.invoice_add_labour_line),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                OutlinedButton(
                    onClick = onAddMaterialsLine,
                    enabled = !materialsAlreadyAdded,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.invoice_add_materials_line),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyInvoiceLinesCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Text(
            text = stringResource(R.string.invoice_no_lines),
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/** A single invoice line card — shows description, qty/rate/amount, and an Edit button. */
@Composable
private fun InvoiceLineCard(
    line: InvoiceLineEntity,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onEdit),
        colors = CardDefaults.cardColors(containerColor = OxfordBlue),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Box {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .matchParentSize()
                    .background(GoldAccent.copy(alpha = 0.5f))
            )
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp + 4.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
            ) {
                val compactLayout = isCompactPhoneWidth(maxWidth)

                if (compactLayout) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = line.description,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (line.isManualAmount) {
                                Text(
                                    text = "Manual amount",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.6f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            } else {
                                Text(
                                    text = "${line.qty} × ${CurrencyFormatUtils.formatCurrency(line.rate)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.6f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = CurrencyFormatUtils.formatCurrency(line.amount),
                                style = MaterialTheme.typography.titleMedium,
                                color = GoldAccent,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(
                                onClick = onEdit,
                                modifier = Modifier.widthIn(min = MinActionButtonWidth),
                                colors = ButtonDefaults.textButtonColors(contentColor = GoldAccent)
                            ) {
                                Text(
                                    text = stringResource(R.string.invoice_edit),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = line.description,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (line.isManualAmount) {
                                Text(
                                    text = "Manual amount",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.6f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            } else {
                                Text(
                                    text = "${line.qty} × ${CurrencyFormatUtils.formatCurrency(line.rate)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.6f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.defaultMinSize(minWidth = 96.dp)
                        ) {
                            Text(
                                text = CurrencyFormatUtils.formatCurrency(line.amount),
                                style = MaterialTheme.typography.titleMedium,
                                color = GoldAccent,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            TextButton(
                                onClick = onEdit,
                                modifier = Modifier.widthIn(min = MinActionButtonWidth),
                                colors = ButtonDefaults.textButtonColors(contentColor = GoldAccent)
                            ) {
                                Text(
                                    text = stringResource(R.string.invoice_edit),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InvoiceOtherAmountLineCard(otherAmount: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = OxfordBlue),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Box {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .matchParentSize()
                    .background(Color.White.copy(alpha = 0.2f))
            )
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp + 4.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
            ) {
                val compactLayout = isCompactPhoneWidth(maxWidth)

                if (compactLayout) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stringResource(R.string.invoice_other_amount),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = CurrencyFormatUtils.formatCurrency(otherAmount),
                            style = MaterialTheme.typography.titleMedium,
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.invoice_other_amount),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = CurrencyFormatUtils.formatCurrency(otherAmount),
                            style = MaterialTheme.typography.titleMedium,
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

/**
 * Shows the full breakdown: subtotal, other amount, optional GST, final total.
 * Uses a divider before the final total for a professional look.
 */
@Composable
private fun InvoiceTotalsCard(
    invoice: InvoiceEntity,
    totals: InvoiceTotals
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = OxfordBlue
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.invoice_totals_header),
                style = MaterialTheme.typography.titleMedium,
                color = GoldAccent,
                fontWeight = FontWeight.Bold
            )

            TotalsRow(
                label = stringResource(R.string.invoice_subtotal),
                value = CurrencyFormatUtils.formatCurrency(totals.subtotalExGst)
            )

            if (totals.otherAmount != 0.0) {
                TotalsRow(
                    label = stringResource(R.string.invoice_other_amount),
                    value = CurrencyFormatUtils.formatCurrency(totals.otherAmount)
                )
            }

            if (invoice.includeGst) {
                TotalsRow(
                    label = "${stringResource(R.string.invoice_gst_amount)} (${InvoiceUtils.formatGstRate(invoice.gstRate)})",
                    value = CurrencyFormatUtils.formatCurrency(totals.gstAmount)
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = Color.White.copy(alpha = 0.2f)
            )

            TotalsRow(
                label = stringResource(R.string.invoice_final_total),
                value = CurrencyFormatUtils.formatCurrency(totals.finalTotal),
                isBold = true,
                isTotal = true
            )
        }
    }
}

/** Helper row with a label on the left and a value on the right. */
@Composable
private fun TotalsRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    isTotal: Boolean = false
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val compactLayout = isCompactPhoneWidth(maxWidth)

        if (compactLayout) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = label,
                    style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
                    color = if (isTotal) Color.White else Color.White.copy(alpha = 0.7f),
                    fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = value,
                    style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
                    color = if (isTotal) GoldAccent else Color.White.copy(alpha = 0.7f),
                    fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = label,
                    style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
                    color = if (isTotal) Color.White else Color.White.copy(alpha = 0.7f),
                    fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = value,
                    style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
                    color = if (isTotal) GoldAccent else Color.White.copy(alpha = 0.7f),
                    fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1
                )
            }
        }
    }
}

/** Small helper composable for label + value pairs inside the header card. */
@Composable
private fun LabelValue(
    label: String,
    value: String,
    stacked: Boolean = false,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    if (stacked) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "$label:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = color.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = color,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "$label:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = color.copy(alpha = 0.7f),
                maxLines = 1
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Invoice Header Dialog
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Dialog for creating or editing the invoice header.
 *
 * Features:
 *  - Invoice number field (read-only, auto-generated)
 *  - Bill To field with live client auto-suggestions
 *  - Issue date field
 *  - GST toggle (Switch)
 *  - Validation error message
 */
@Composable
fun InvoiceHeaderDialog(
    formState: InvoiceHeaderFormState,
    clientSuggestions: List<ClientEntity>,
    onDismiss: () -> Unit,
    onInvoiceNumberChange: (String) -> Unit,
    onBillToClientSelected: (ClientEntity) -> Unit,
    onIssueDateChange: (String) -> Unit,
    onIncludeGstChange: (Boolean) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    var billToExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Title
                Text(
                    text = if (formState.invoiceId == null) {
                        stringResource(R.string.invoice_new_header)
                    } else {
                        stringResource(R.string.invoice_edit_header)
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                // Invoice number
                OutlinedTextField(
                    value = formState.invoiceNumber,
                    onValueChange = {},
                    label = { Text(stringResource(R.string.invoice_number)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    readOnly = true
                )

                // Bill To dropdown (single selector for saved clients)
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = formState.billToName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.invoice_bill_to)) },
                        placeholder = { Text(stringResource(R.string.invoice_bill_to_hint)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { billToExpanded = true },
                        singleLine = true
                    )

                    DropdownMenu(
                        expanded = billToExpanded,
                        onDismissRequest = { billToExpanded = false }
                    ) {
                        clientSuggestions.forEach { client ->
                            DropdownMenuItem(
                                text = { Text(client.name) },
                                onClick = {
                                    onBillToClientSelected(client)
                                    billToExpanded = false
                                }
                            )
                        }
                    }
                }

                // Issue date
                OutlinedTextField(
                    value = formState.issueDate,
                    onValueChange = onIssueDateChange,
                    label = { Text(stringResource(R.string.invoice_date)) },
                    supportingText = { Text(stringResource(R.string.invoice_date_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // GST toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.invoice_include_gst),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = formState.includeGst,
                        onCheckedChange = onIncludeGstChange
                    )
                }

                // Validation error
                formState.errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Action buttons
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val compactLayout = isCompactPhoneWidth(maxWidth)

                    if (compactLayout) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            TextButton(onClick = onSave) {
                                Text(text = stringResource(R.string.invoice_save))
                            }
                            TextButton(onClick = onDismiss) {
                                Text(text = stringResource(R.string.invoice_cancel))
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = onDismiss) {
                                Text(text = stringResource(R.string.invoice_cancel))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(onClick = onSave) {
                                Text(text = stringResource(R.string.invoice_save))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Invoice Line Dialog
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Dialog for adding or editing a single invoice line.
 *
 * Two modes:
 *  - **Auto amount** (default): user enters Qty and Rate; Amount = Qty × Rate (read-only preview).
 *  - **Manual amount**: user directly types the Amount (useful for lump-sum material totals).
 *
 * The mode is toggled via a Switch labelled "Override amount manually".
 */
@Composable
fun InvoiceLineDialog(
    formState: InvoiceLineFormState,
    onDismiss: () -> Unit,
    onDescriptionChange: (String) -> Unit,
    onQtyChange: (String) -> Unit,
    onRateChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onManualAmountChange: (Boolean) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Title
                Text(
                    text = if (formState.lineId == null) {
                        stringResource(R.string.invoice_add_line)
                    } else {
                        stringResource(R.string.invoice_edit_line)
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                // Description
                OutlinedTextField(
                    value = formState.description,
                    onValueChange = onDescriptionChange,
                    label = { Text(stringResource(R.string.invoice_line_description)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Manual amount toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.invoice_line_manual_amount),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = formState.isManualAmount,
                        onCheckedChange = onManualAmountChange
                    )
                }

                if (formState.isManualAmount) {
                    // ── Manual amount mode ─────────────────────────────────
                    OutlinedTextField(
                        value = formState.amountText,
                        onValueChange = onAmountChange,
                        label = { Text(stringResource(R.string.invoice_line_amount)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    // Still allow optional qty/rate context (greyed out)
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val compactLayout = isCompactPhoneWidth(maxWidth)

                        if (compactLayout) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = formState.qtyText,
                                    onValueChange = onQtyChange,
                                    label = { Text(stringResource(R.string.invoice_line_qty)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    enabled = false
                                )
                                OutlinedTextField(
                                    value = formState.rateText,
                                    onValueChange = onRateChange,
                                    label = { Text(stringResource(R.string.invoice_line_rate)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    enabled = false
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = formState.qtyText,
                                    onValueChange = onQtyChange,
                                    label = { Text(stringResource(R.string.invoice_line_qty)) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    enabled = false
                                )
                                OutlinedTextField(
                                    value = formState.rateText,
                                    onValueChange = onRateChange,
                                    label = { Text(stringResource(R.string.invoice_line_rate)) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    enabled = false
                                )
                            }
                        }
                    }
                } else {
                    // ── Auto-calculated amount mode ────────────────────────
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val compactLayout = isCompactPhoneWidth(maxWidth)

                        if (compactLayout) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = formState.qtyText,
                                    onValueChange = onQtyChange,
                                    label = { Text(stringResource(R.string.invoice_line_qty)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                )
                                OutlinedTextField(
                                    value = formState.rateText,
                                    onValueChange = onRateChange,
                                    label = { Text(stringResource(R.string.invoice_line_rate)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = formState.qtyText,
                                    onValueChange = onQtyChange,
                                    label = { Text(stringResource(R.string.invoice_line_qty)) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                )
                                OutlinedTextField(
                                    value = formState.rateText,
                                    onValueChange = onRateChange,
                                    label = { Text(stringResource(R.string.invoice_line_rate)) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                )
                            }
                        }
                    }

                    // Auto-calculated amount preview
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = stringResource(R.string.invoice_line_amount))
                            Text(
                                text = formState.effectiveAmount
                                    ?.let { CurrencyFormatUtils.formatCurrency(it) }
                                    ?: "--",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Validation error
                formState.errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Action buttons
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val compactLayout = isCompactPhoneWidth(maxWidth)

                    if (compactLayout) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            TextButton(onClick = onSave) {
                                Text(text = stringResource(R.string.invoice_save))
                            }
                            TextButton(onClick = onDismiss) {
                                Text(text = stringResource(R.string.invoice_cancel))
                            }
                            if (formState.lineId != null) {
                                TextButton(onClick = onDelete) {
                                    Text(text = stringResource(R.string.invoice_delete))
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (formState.lineId != null) {
                                TextButton(onClick = onDelete) {
                                    Text(text = stringResource(R.string.invoice_delete))
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            TextButton(onClick = onDismiss) {
                                Text(text = stringResource(R.string.invoice_cancel))
                            }
                            TextButton(onClick = onSave) {
                                Text(text = stringResource(R.string.invoice_save))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@PreviewScreenSizes
@Composable
private fun InvoiceScreenPreview() {
    ProPaintersPlasterersPaymentTheme {
        InvoiceScreen(
            uiState = InvoiceUiState(
                isLoading = false,
                job = JobEntity(
                    jobId = 1,
                    propertyAddress = "12 King Street, Sydney",
                    jobName = "Interior repaint"
                ),
                invoice = InvoiceEntity(
                    invoiceId = 1,
                    invoiceNumber = "INV-ABC123456",
                    jobId = 1,
                    clientId = 1,
                    invoiceDate = "2026-04-09",
                    billToNameSnapshot = "Acme Corp",
                    billToAddressSnapshot = "123 Main St",
                    billToPhoneSnapshot = "0200000000",
                    billToEmailSnapshot = "accounts@acme.com",
                    subtotalExclusiveGst = 1369.95,
                    gstEnabled = true,
                    gstRate = 0.10,
                    gstAmount = 136.995,
                    otherAmount = 120.0,
                    totalAmount = 1638.945,
                    notes = "Payment due 14 days."
                ),
                lines = listOf(
                    InvoiceLineEntity(
                        invoiceLineId = 1,
                        invoiceId = 1,
                        description = "Labour",
                        qty = 16.0,
                        rate = 65.0,
                        amount = 1040.0,
                        manualAmountOverride = false,
                        sortOrder = 1
                    ),
                    InvoiceLineEntity(
                        invoiceLineId = 2,
                        invoiceId = 1,
                        description = "Materials",
                        qty = 1.0,
                        rate = 329.95,
                        amount = 329.95,
                        manualAmountOverride = true,
                        sortOrder = 2
                    )
                ),
                totals = InvoiceTotals(
                    subtotalExGst = 1369.95,
                    gstAmount = 148.995,
                    subtotalIncGst = 1638.945,
                    otherAmount = 120.0,
                    finalTotal = 1638.945
                )
            ),
            onExportPdf = {},
            onCreateInvoice = {},
            onEditHeader = {},
            onDismissHeader = {},
            onInvoiceNumberChange = {},
            onBillToClientSelected = {},
            onIssueDateChange = {},
            onIncludeGstChange = {},
            onOtherAmountChange = {},
            onNotesChange = {},
            onSaveHeader = {},
            onAddLine = {},
            onEditLine = {},
            onDismissLine = {},
            onLineDescriptionChange = {},
            onLineQtyChange = {},
            onLineRateChange = {},
            onLineAmountChange = {},
            onLineManualAmountChange = {},
            onSaveLine = {},
            onDeleteLine = {},
            onAddLabourLine = {},
            onAddMaterialsLine = {},
            onMessageShown = {}
        )
    }
}

