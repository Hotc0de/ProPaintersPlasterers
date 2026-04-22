package com.example.propaintersplastererspayment.feature.timesheet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.propaintersplastererspayment.ProPaintersApplication
import com.example.propaintersplastererspayment.R
import com.example.propaintersplastererspayment.core.pdf.PdfExportService
import com.example.propaintersplastererspayment.core.pdf.PdfFileHelper
import com.example.propaintersplastererspayment.core.util.DateFormatUtils
import com.example.propaintersplastererspayment.core.util.WorkEntryTimeUtils
import com.example.propaintersplastererspayment.data.local.entity.WorkEntryEntity
import com.example.propaintersplastererspayment.feature.timesheet.ui.luxury.TimesheetLuxuryPreviewPaging
import com.example.propaintersplastererspayment.feature.timesheet.vm.TimesheetUiState
import com.example.propaintersplastererspayment.feature.timesheet.vm.TimesheetViewModel
import com.example.propaintersplastererspayment.feature.timesheet.vm.WorkEntryFormState
import com.example.propaintersplastererspayment.ui.components.*
import com.example.propaintersplastererspayment.ui.theme.*

@Composable
fun TimesheetRoute(
    jobId: Long,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val application = context.applicationContext as ProPaintersApplication
    val pdfExportService = remember { PdfExportService() }
    val pdfShareTitle = stringResource(R.string.pdf_share_timesheet)
    val viewModel: TimesheetViewModel = viewModel(
        factory = TimesheetViewModel.provideFactory(
            jobId = jobId,
            jobRepository = application.container.jobRepository,
            workEntryRepository = application.container.workEntryRepository,
            materialRepository = application.container.materialRepository,
            settingsRepository = application.container.settingsRepository
        )
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.pdfExportEvents.collect { exportData ->
            runCatching {
                val outputFile = PdfFileHelper.createExportFile(context, exportData.fileName)
                pdfExportService.exportTimesheetPdf(exportData, outputFile)
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

    TimesheetScreen(
        uiState = uiState,
        modifier = modifier,
        onAddEntry = viewModel::openAddEntry,
        onExportPdf = viewModel::exportTimesheetPdf,
        onEditEntry = viewModel::openEditEntry,
        onDismissForm = viewModel::dismissForm,
        onWorkDateChange = viewModel::onWorkDateChange,
        onWorkerNameChange = viewModel::onWorkerNameChange,
        onStartTimeChange = viewModel::onStartTimeChange,
        onFinishTimeChange = viewModel::onFinishTimeChange,
        onSaveEntry = viewModel::saveEntry,
        onDeleteEntry = viewModel::deleteEntry,
        onToggleLuxuryPreview = viewModel::toggleLuxuryPreview,
        onMessageShown = viewModel::clearUserMessage
    )
}

@Composable
fun TimesheetScreen(
    uiState: TimesheetUiState,
    onAddEntry: () -> Unit,
    onExportPdf: () -> Unit,
    onEditEntry: (WorkEntryEntity) -> Unit,
    onDismissForm: () -> Unit,
    onWorkDateChange: (String) -> Unit,
    onWorkerNameChange: (String) -> Unit,
    onStartTimeChange: (TextFieldValue) -> Unit,
    onFinishTimeChange: (TextFieldValue) -> Unit,
    onSaveEntry: () -> Unit,
    onDeleteEntry: (Long) -> Unit,
    onToggleLuxuryPreview: () -> Unit,
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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            if (uiState.job != null) {
                IndustrialFAB(onClick = onAddEntry)
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
                            text = stringResource(R.string.timesheet_no_job),
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextMuted
                        )
                    }
                }

                else -> {
                    if (uiState.isLuxuryPreviewMode) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Luxury Preview",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = IndustrialGold
                                )
                                TextButton(onClick = onToggleLuxuryPreview) {
                                    Text("Go Back", color = IndustrialGold)
                                }
                            }
                            TimesheetLuxuryPreviewPaging(
                                uiState = uiState
                            )
                            /*
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                TimesheetLuxuryPreview(
                                    uiState = uiState
                                )
                            }
                            */
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                TotalHoursHeroCard(totalHours = uiState.totalHours)
                            }

                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    SecondaryButton(
                                        text = stringResource(R.string.pdf_export_timesheet),
                                        onClick = onExportPdf,
                                        modifier = Modifier.weight(1f),
                                        icon = {
                                            Icon(
                                                Icons.Default.PictureAsPdf,
                                                contentDescription = null,
                                                tint = IndustrialGold,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    )
                                    SecondaryButton(
                                        text = "Preview",
                                        onClick = onToggleLuxuryPreview,
                                        modifier = Modifier.weight(1f),
                                        icon = {
                                            Icon(
                                                Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = IndustrialGold,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    )
                                }
                            }

                            if (uiState.entries.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = stringResource(R.string.timesheet_no_entries),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextSubdued
                                        )
                                    }
                                }
                            } else {
                                items(uiState.entries, key = { it.entryId }) { entry ->
                                    WorkEntryCard(
                                        entry = entry,
                                        onClick = { onEditEntry(entry) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (uiState.isFormVisible) {
        WorkEntryFormDialog(
            formState = uiState.formState,
            onDismiss = onDismissForm,
            onWorkDateChange = onWorkDateChange,
            onWorkerNameChange = onWorkerNameChange,
            onStartTimeChange = onStartTimeChange,
            onFinishTimeChange = onFinishTimeChange,
            onSave = onSaveEntry,
            onDelete = {
                uiState.formState.entryId?.let(onDeleteEntry)
            }
        )
    }
}

@Composable
private fun TotalHoursHeroCard(totalHours: Double) {
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
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = CharcoalBackground,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.timesheet_total_hours),
                        style = MaterialTheme.typography.labelLarge,
                        color = CharcoalBackground
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = WorkEntryTimeUtils.formatHours(totalHours),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Black,
                    color = CharcoalBackground
                )
            }
        }
    }
}

@Composable
private fun WorkEntryCard(entry: WorkEntryEntity, onClick: () -> Unit) {
    IndustrialCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = AppShapes.medium,
                color = IndustrialGold.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = IndustrialGold,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.workerName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = OffWhite
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text(
                            text = "Date",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSubdued
                        )
                        Text(
                            text = DateFormatUtils.formatDisplayDate(entry.workDate),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = OffWhite
                        )
                    }
                    Column {
                        Text(
                            text = "Hours",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSubdued
                        )
                        Text(
                            text = WorkEntryTimeUtils.formatHours(entry.hoursWorked),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = IndustrialGold
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = TextMuted
            )
            Text(
                text = "${entry.startTime}  →  ${entry.finishTime}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )
        }
    }
}

@Composable
fun WorkEntryFormDialog(
    formState: WorkEntryFormState,
    onDismiss: () -> Unit,
    onWorkDateChange: (String) -> Unit,
    onWorkerNameChange: (String) -> Unit,
    onStartTimeChange: (TextFieldValue) -> Unit,
    onFinishTimeChange: (TextFieldValue) -> Unit,
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (formState.entryId == null) "Add Work Entry" else "Edit Work Entry",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = OffWhite
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                IndustrialTextField(
                    value = formState.workerName,
                    onValueChange = onWorkerNameChange,
                    label = stringResource(R.string.timesheet_worker_name),
                    placeholder = "Worker Name"
                )

                IndustrialTextField(
                    value = formState.workDate,
                    onValueChange = onWorkDateChange,
                    label = stringResource(R.string.timesheet_work_date),
                    placeholder = "dd-MM-yyyy"
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IndustrialTextField(
                        value = formState.startTime,
                        onValueChange = onStartTimeChange,
                        label = stringResource(R.string.timesheet_start_time),
                        placeholder = "08:00",
                        modifier = Modifier.weight(1f)
                    )
                    IndustrialTextField(
                        value = formState.finishTime,
                        onValueChange = onFinishTimeChange,
                        label = stringResource(R.string.timesheet_finish_time),
                        placeholder = "17:00",
                        modifier = Modifier.weight(1f)
                    )
                }

                formState.errorMessage?.let {
                    Text(text = it, color = ErrorRed, style = MaterialTheme.typography.bodySmall)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (formState.entryId != null) {
                        var showConfirm by remember { mutableStateOf(false) }
                        OutlinedButton(
                            onClick = { showConfirm = true },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = AppShapes.large,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f))
                        ) {
                            Text("Delete", fontWeight = FontWeight.Bold)
                        }
                        if (showConfirm) {
                            ConfirmDeleteDialog(
                                title = "Delete Entry",
                                message = "Are you sure you want to delete this work entry?",
                                onConfirm = {
                                    showConfirm = false
                                    onDelete()
                                },
                                onDismiss = { showConfirm = false }
                            )
                        }
                    }
                    PrimaryButton(
                        text = "Save",
                        onClick = onSave,
                        modifier = Modifier.weight(1f)
                    )
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
    icon: @Composable (() -> Unit)? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp).fillMaxWidth(),
        shape = AppShapes.large,
        colors = ButtonDefaults.buttonColors(
            containerColor = CharcoalSecondary,
            contentColor = OffWhite
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.invoke()
            if (icon != null) Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
