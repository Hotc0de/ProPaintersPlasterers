package com.example.propaintersplastererspayment.feature.invoice.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.propaintersplastererspayment.ProPaintersApplication
import com.example.propaintersplastererspayment.R
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
import com.example.propaintersplastererspayment.ui.components.*
import com.example.propaintersplastererspayment.ui.theme.*

@Composable
fun InvoiceRoute(
    jobId: Long,
    isQuickInvoice: Boolean = false,
    onBack: (() -> Unit)? = null,
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
    val pdfShareTitle = stringResource(R.string.pdf_share_invoice)

    LaunchedEffect(viewModel) {
        viewModel.pdfExportEvents.collect { exportData ->
            runCatching {
                val outputFile = PdfFileHelper.createExportFile(context, exportData.fileName)
                // Use a fresh PdfExportService instance here to avoid any compose-captured type inference
                // issues during compilation in this debug-only path.
                PdfExportService().exportInvoicePdf(exportData, outputFile)
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
        isQuickInvoice = isQuickInvoice,
        onBack = onBack,
        modifier = modifier,
        onExportPdf = viewModel::exportInvoicePdf,
        onCreateInvoice = viewModel::openCreateInvoice,
        onEditHeader = viewModel::openEditHeader,
        onDismissHeader = viewModel::dismissHeader,
        onInvoiceNumberChange = viewModel::onInvoiceNumberChange,
        onBillToNameChange = viewModel::onBillToNameChange,
        onBillToClientSelected = viewModel::onBillToClientSelected,
        onIssueDateChange = viewModel::onIssueDateChange,
        onDueDateChange = viewModel::onDueDateChange,
        onIncludeDueDateChange = viewModel::onIncludeDueDateChange,
        onIncludeGstChange = viewModel::onIncludeGstChange,
        onNotesChange = viewModel::onNotesChange,
        onSaveHeader = viewModel::saveHeader,
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
        onAddLabourLine = viewModel::openAddLabourLine,
        onAddMaterialsLine = viewModel::openAddMaterialsLine,
        onMarkAsPaid = viewModel::markAsPaid,
        onMessageShown = viewModel::clearUserMessage
    )
}

@Composable
fun InvoiceScreen(
    uiState: InvoiceUiState,
    isQuickInvoice: Boolean,
    onBack: (() -> Unit)? = null,
    onExportPdf: () -> Unit,
    onCreateInvoice: () -> Unit,
    onEditHeader: (InvoiceEntity) -> Unit,
    onDismissHeader: () -> Unit,
    onInvoiceNumberChange: (TextFieldValue) -> Unit,
    onBillToNameChange: (TextFieldValue) -> Unit,
    onBillToClientSelected: (ClientEntity) -> Unit,
    onIssueDateChange: (TextFieldValue) -> Unit,
    onDueDateChange: (TextFieldValue) -> Unit,
    onIncludeDueDateChange: (Boolean) -> Unit,
    onIncludeGstChange: (Boolean) -> Unit,
    onNotesChange: (TextFieldValue) -> Unit,
    onSaveHeader: () -> Unit,
    onAddLine: () -> Unit,
    onEditLine: (InvoiceLineEntity) -> Unit,
    onDismissLine: () -> Unit,
    onLineDescriptionChange: (TextFieldValue) -> Unit,
    onLineQtyChange: (TextFieldValue) -> Unit,
    onLineRateChange: (TextFieldValue) -> Unit,
    onLineAmountChange: (TextFieldValue) -> Unit,
    onLineManualAmountChange: (Boolean) -> Unit,
    onSaveLine: () -> Unit,
    onDeleteLine: (Long) -> Unit,
    onAddLabourLine: () -> Unit,
    onAddMaterialsLine: () -> Unit,
    onMarkAsPaid: () -> Unit,
    onMessageShown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onMessageShown()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CharcoalBackground,
        topBar = {
            if (onBack != null) {
                val screenTitle = uiState.job?.clientNameSnapshot?.takeIf { it.isNotBlank() }
                    ?: stringResource(R.string.invoice_title)
                
                Column(
                    modifier = Modifier
                        .background(CharcoalBackground)
                        .padding(top = 16.dp, start = 8.dp, end = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = IndustrialGold
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = screenTitle,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = IndustrialGold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            uiState.job?.propertyAddress?.takeIf { it.isNotBlank() }?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            if (uiState.invoice != null) {
                IndustrialFAB(onClick = onAddLine)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = AppDimensions.screenPadding)
        ) {
            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = IndustrialGold)
                    }
                }

                uiState.job == null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.invoice_no_job),
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextMuted
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            InvoiceTotalsHeroCard(totals = uiState.totals, invoice = uiState.invoice)
                        }

                        item {
                            if (uiState.invoice == null) {
                                NoInvoiceIndustrialCard(onCreateInvoice = onCreateInvoice)
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    InvoiceHeaderIndustrialCard(
                                        invoice = uiState.invoice,
                                        onExportPdf = onExportPdf,
                                        onEdit = { onEditHeader(uiState.invoice) }
                                    )

                                    if (uiState.job.status == com.example.propaintersplastererspayment.data.local.entity.JobStatus.WAITING_FOR_PAYMENT) {
                                        PrimaryButton(
                                            text = "Mark as Paid",
                                            onClick = onMarkAsPaid,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }

                        if (uiState.invoice != null) {
                            item {
                                ImportActionsRow(
                                    onAddLabourLine = onAddLabourLine,
                                    onAddMaterialsLine = onAddMaterialsLine,
                                    labourAlreadyAdded = uiState.hasImportedLabourLine || isQuickInvoice,
                                    materialsAlreadyAdded = uiState.hasImportedMaterialsLine || isQuickInvoice
                                )
                            }

                            if (uiState.lines.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = stringResource(R.string.invoice_no_lines),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextSubdued
                                        )
                                    }
                                }
                            } else {
                                items(uiState.lines, key = { it.lineId }) { line ->
                                    InvoiceLineIndustrialCard(
                                        line = line,
                                        onEdit = { onEditLine(line) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (uiState.isEditingHeader) {
        InvoiceHeaderDialog(
            formState = uiState.headerFormState,
            clientSuggestions = uiState.clientSuggestions,
            onDismiss = onDismissHeader,
            onInvoiceNumberChange = onInvoiceNumberChange,
            onBillToNameChange = onBillToNameChange,
            onBillToClientSelected = onBillToClientSelected,
            onIssueDateChange = onIssueDateChange,
            onDueDateChange = onDueDateChange,
            onIncludeDueDateChange = onIncludeDueDateChange,
            onIncludeGstChange = onIncludeGstChange,
            onNotesChange = onNotesChange,
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

@Composable
private fun InvoiceTotalsHeroCard(totals: InvoiceTotals, invoice: InvoiceEntity?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.large,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(IndustrialGold, IndustrialGoldDark)
                    ),
                    shape = AppShapes.large
                )
                .padding(AppDimensions.cardPadding)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = CharcoalBackground,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.invoice_final_total),
                        style = MaterialTheme.typography.labelLarge,
                        color = CharcoalBackground
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = CurrencyFormatUtils.formatCurrency(totals.finalTotal),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Black,
                    color = CharcoalBackground
                )
                if (invoice?.includeGst == true) {
                    Text(
                        text = "Includes ${CurrencyFormatUtils.formatCurrency(totals.gstAmount)} GST",
                        style = MaterialTheme.typography.bodySmall,
                        color = CharcoalBackground.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun NoInvoiceIndustrialCard(onCreateInvoice: () -> Unit) {
    IndustrialCard(onClick = onCreateInvoice) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PostAdd,
                contentDescription = null,
                tint = IndustrialGold,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = stringResource(R.string.invoice_no_invoice),
                style = MaterialTheme.typography.bodyLarge,
                color = OffWhite,
                fontWeight = FontWeight.Bold
            )
            PrimaryButton(
                text = stringResource(R.string.invoice_create),
                onClick = onCreateInvoice
            )
        }
    }
}

@Composable
private fun InvoiceHeaderIndustrialCard(
    invoice: InvoiceEntity,
    onExportPdf: () -> Unit,
    onEdit: () -> Unit
) {
    IndustrialCard(onClick = onEdit) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = invoice.invoiceNumber,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = OffWhite
                    )
                    Text(
                        text = DateFormatUtils.formatDisplayDate(invoice.issueDate),
                        style = MaterialTheme.typography.bodyMedium,
                        color = IndustrialGold
                    )
                    invoice.dueDate?.let { dueDate ->
                        Text(
                            text = "Due: ${DateFormatUtils.formatDisplayDate(dueDate)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
                IconButton(onClick = onExportPdf) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF", tint = IndustrialGold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = BorderColor)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Bill To", style = MaterialTheme.typography.bodySmall, color = TextSubdued)
                    Text(text = invoice.billToName, style = MaterialTheme.typography.bodyMedium, color = OffWhite, fontWeight = FontWeight.Bold)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "GST Status", style = MaterialTheme.typography.bodySmall, color = TextSubdued)
                    Text(
                        text = if (invoice.includeGst) "Registered" else "No GST",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (invoice.includeGst) SuccessGreen else TextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun ImportActionsRow(
    onAddLabourLine: () -> Unit,
    onAddMaterialsLine: () -> Unit,
    labourAlreadyAdded: Boolean,
    materialsAlreadyAdded: Boolean
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SecondaryButton(
            text = "Import Labour",
            onClick = onAddLabourLine,
            modifier = Modifier.weight(1f),
            enabled = !labourAlreadyAdded,
            icon = { Icon(Icons.Default.Groups, null, modifier = Modifier.size(18.dp)) }
        )
        SecondaryButton(
            text = "Import Materials",
            onClick = onAddMaterialsLine,
            modifier = Modifier.weight(1f),
            enabled = !materialsAlreadyAdded,
            icon = { Icon(Icons.Default.Inventory2, null, modifier = Modifier.size(18.dp)) }
        )
    }
}

@Composable
private fun InvoiceLineIndustrialCard(
    line: InvoiceLineEntity,
    onEdit: () -> Unit
) {
    IndustrialCard(onClick = onEdit) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = line.description,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OffWhite
                )
                if (!line.isManualAmount) {
                    Text(
                        text = "${line.qty} × ${CurrencyFormatUtils.formatCurrency(line.rate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSubdued
                    )
                } else {
                    Text(
                        text = "Manual Entry",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSubdued
                    )
                }
            }
            Text(
                text = CurrencyFormatUtils.formatCurrency(line.amount),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = IndustrialGold
            )
        }
    }
}

@Composable
fun InvoiceHeaderDialog(
    formState: InvoiceHeaderFormState,
    clientSuggestions: List<ClientEntity>,
    onDismiss: () -> Unit,
    onInvoiceNumberChange: (TextFieldValue) -> Unit,
    onBillToNameChange: (TextFieldValue) -> Unit,
    onBillToClientSelected: (ClientEntity) -> Unit,
    onIssueDateChange: (TextFieldValue) -> Unit,
    onDueDateChange: (TextFieldValue) -> Unit,
    onIncludeDueDateChange: (Boolean) -> Unit,
    onIncludeGstChange: (Boolean) -> Unit,
    onNotesChange: (TextFieldValue) -> Unit,
    onSave: () -> Unit
) {
    var billToExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.large,
            colors = CardDefaults.cardColors(containerColor = CharcoalCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = if (formState.invoiceId == null) "New Invoice Header" else "Edit Invoice Header",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = OffWhite
                )

                IndustrialTextField(
                    value = formState.invoiceNumber,
                    onValueChange = onInvoiceNumberChange,
                    label = stringResource(R.string.invoice_number),
                    readOnly = true
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    IndustrialTextField(
                        value = formState.billToName,
                        onValueChange = onBillToNameChange,
                        label = stringResource(R.string.invoice_bill_to),
                        placeholder = "Select Client",
                        trailingIcon = {
                            IconButton(onClick = { billToExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, null, tint = IndustrialGold)
                            }
                        }
                    )

                    DropdownMenu(
                        expanded = billToExpanded,
                        onDismissRequest = { billToExpanded = false },
                        modifier = Modifier.background(CharcoalCard)
                    ) {
                        clientSuggestions.forEach { client ->
                            DropdownMenuItem(
                                text = { Text(client.name, color = OffWhite) },
                                onClick = {
                                    onBillToClientSelected(client)
                                    billToExpanded = false
                                }
                            )
                        }
                    }
                }

                IndustrialTextField(
                    value = formState.issueDate,
                    onValueChange = onIssueDateChange,
                    label = stringResource(R.string.invoice_date),
                    placeholder = "dd-MM-yyyy"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Include Due Date",
                        color = OffWhite,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = formState.includeDueDate,
                        onCheckedChange = onIncludeDueDateChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = IndustrialGold,
                            checkedTrackColor = IndustrialGold.copy(alpha = 0.5f)
                        )
                    )
                }

                if (formState.includeDueDate) {
                    IndustrialTextField(
                        value = formState.dueDate,
                        onValueChange = onDueDateChange,
                        label = "Due Date",
                        placeholder = "dd-MM-yyyy"
                    )
                }

                IndustrialTextField(
                    value = formState.notes,
                    onValueChange = onNotesChange,
                    label = stringResource(R.string.invoice_notes),
                    placeholder = "Internal notes...",
                    minLines = 3
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.invoice_include_gst),
                        color = OffWhite,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = formState.includeGst,
                        onCheckedChange = onIncludeGstChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = IndustrialGold,
                            checkedTrackColor = IndustrialGold.copy(alpha = 0.5f)
                        )
                    )
                }

                formState.errorMessage?.let {
                    Text(text = it, color = ErrorRed, style = MaterialTheme.typography.bodySmall)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SecondaryButton(text = "Cancel", onClick = onDismiss, modifier = Modifier.weight(1f))
                    PrimaryButton(text = "Save", onClick = onSave, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceLineDialog(
    formState: InvoiceLineFormState,
    onDismiss: () -> Unit,
    onDescriptionChange: (TextFieldValue) -> Unit,
    onQtyChange: (TextFieldValue) -> Unit,
    onRateChange: (TextFieldValue) -> Unit,
    onAmountChange: (TextFieldValue) -> Unit,
    onManualAmountChange: (Boolean) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.large,
            colors = CardDefaults.cardColors(containerColor = CharcoalCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = if (formState.lineId == null) "Add Line Item" else "Edit Line Item",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = OffWhite
                )

                IndustrialTextField(
                    value = formState.description,
                    onValueChange = onDescriptionChange,
                    label = "Description",
                    placeholder = "e.g. Labour"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Manual Amount Override",
                            color = OffWhite,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                                positioning = TooltipAnchorPosition.Above
                            ),
                            tooltip = {
                                PlainTooltip(
                                    containerColor = CharcoalSecondary,
                                    contentColor = OffWhite
                                ) {
                                    Text(
                                        text = "Allows you to enter a total amount directly instead of calculating it from Quantity and Rate.\\n\\nExample: Use this for fixed-price tasks like 'Full Room Painting - $500' without specifying hours or hourly rates.",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            },
                            state = rememberTooltipState()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Help",
                                tint = IndustrialGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Switch(
                        checked = formState.isManualAmount,
                        onCheckedChange = onManualAmountChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = IndustrialGold,
                            checkedTrackColor = IndustrialGold.copy(alpha = 0.5f)
                        )
                    )
                }

                if (formState.isManualAmount) {
                    IndustrialTextField(
                        value = formState.amountText,
                        onValueChange = onAmountChange,
                        label = "Amount",
                        placeholder = "0.00",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                } else {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IndustrialTextField(
                            value = formState.qtyText,
                            onValueChange = onQtyChange,
                            label = "Qty",
                            placeholder = "1.0",
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        IndustrialTextField(
                            value = formState.rateText,
                            onValueChange = onRateChange,
                            label = "Rate",
                            placeholder = "0.00",
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CharcoalSecondary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Subtotal", color = TextMuted)
                            Text(
                                text = formState.effectiveAmount?.let { CurrencyFormatUtils.formatCurrency(it) } ?: "--",
                                color = IndustrialGold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                formState.errorMessage?.let {
                    Text(text = it, color = ErrorRed, style = MaterialTheme.typography.bodySmall)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (formState.lineId != null) {
                        var showConfirm by remember { mutableStateOf(false) }
                        OutlinedButton(
                            onClick = { showConfirm = true },
                            modifier = Modifier.weight(0.5f).height(56.dp),
                            shape = AppShapes.large,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.Delete, null)
                        }
                        if (showConfirm) {
                            com.example.propaintersplastererspayment.ui.components.ConfirmDeleteDialog(
                                title = "Delete Line",
                                message = "Are you sure you want to delete this invoice line?",
                                onConfirm = {
                                    showConfirm = false
                                    onDelete()
                                },
                                onDismiss = { showConfirm = false }
                            )
                        }
                    }
                    SecondaryButton(text = "Cancel", onClick = onDismiss, modifier = Modifier.weight(1f))
                    PrimaryButton(text = "Save", onClick = onSave, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: @Composable (() -> Unit)? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(56.dp).fillMaxWidth(),
        shape = AppShapes.large,
        colors = ButtonDefaults.buttonColors(
            containerColor = CharcoalSecondary,
            contentColor = OffWhite,
            disabledContainerColor = CharcoalMuted,
            disabledContentColor = TextSubdued
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.invoke()
            if (icon != null) Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}