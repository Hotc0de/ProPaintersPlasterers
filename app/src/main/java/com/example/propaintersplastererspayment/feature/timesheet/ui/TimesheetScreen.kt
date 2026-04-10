package com.example.propaintersplastererspayment.feature.timesheet.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.propaintersplastererspayment.ProPaintersApplication
import com.example.propaintersplastererspayment.R
import com.example.propaintersplastererspayment.core.pdf.PdfExportService
import com.example.propaintersplastererspayment.core.pdf.PdfFileHelper
import com.example.propaintersplastererspayment.core.util.DateFormatUtils
import com.example.propaintersplastererspayment.core.util.WorkEntryTimeUtils
import com.example.propaintersplastererspayment.data.local.entity.JobEntity
import com.example.propaintersplastererspayment.data.local.entity.WorkEntryEntity
import com.example.propaintersplastererspayment.feature.timesheet.vm.TimesheetUiState
import com.example.propaintersplastererspayment.feature.timesheet.vm.TimesheetViewModel
import com.example.propaintersplastererspayment.feature.timesheet.vm.WorkEntryFormState
import com.example.propaintersplastererspayment.ui.theme.ProPaintersPlasterersPaymentTheme

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
                pdfExportService.exportTimesheetPdf(context, exportData, outputFile)
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
    onStartTimeChange: (String) -> Unit,
    onFinishTimeChange: (String) -> Unit,
    onSaveEntry: () -> Unit,
    onDeleteEntry: (Long) -> Unit,
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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            if (uiState.job != null) {
                ExtendedFloatingActionButton(onClick = onAddEntry) {
                    Text(text = stringResource(R.string.timesheet_add_entry))
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
                        text = stringResource(R.string.timesheet_no_job),
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
                    item {
                        JobSummaryCard(job = uiState.job)
                    }
                    item {
                        TotalHoursCard(totalHours = uiState.totalHours)
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(onClick = onExportPdf) {
                                Text(text = stringResource(R.string.pdf_export_timesheet))
                            }
                        }
                    }
                    if (uiState.entries.isEmpty()) {
                        item {
                            EmptyTimesheetCard()
                        }
                    } else {
                        items(uiState.entries, key = { entry -> entry.entryId }) { entry ->
                            WorkEntryCard(
                                entry = entry,
                                onEditEntry = { onEditEntry(entry) }
                            )
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
private fun JobSummaryCard(job: JobEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = job.clientName.ifBlank {
                    job.jobName.ifBlank { stringResource(R.string.timesheet_unknown_job) }
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${stringResource(R.string.timesheet_job_address)}: ${job.propertyAddress}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun TotalHoursCard(totalHours: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.timesheet_total_hours),
                style = MaterialTheme.typography.titleMedium
            )
            AssistChip(
                onClick = {},
                label = {
                    Text(text = WorkEntryTimeUtils.formatHours(totalHours))
                }
            )
        }
    }
}

@Composable
private fun EmptyTimesheetCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.timesheet_no_entries),
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun WorkEntryCard(
    entry: WorkEntryEntity,
    onEditEntry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onEditEntry)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.workerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onEditEntry) {
                    Text(text = stringResource(R.string.timesheet_edit))
                }
            }
            Text(
                text = DateFormatUtils.formatDisplayDate(entry.workDate),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${entry.startTime} - ${entry.finishTime}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${stringResource(R.string.timesheet_hours_worked)}: ${WorkEntryTimeUtils.formatHours(entry.hoursWorked)}",
                style = MaterialTheme.typography.bodyMedium
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
    onStartTimeChange: (String) -> Unit,
    onFinishTimeChange: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (formState.entryId == null) {
                        stringResource(R.string.timesheet_add_entry)
                    } else {
                        stringResource(R.string.timesheet_edit_entry)
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = formState.workDate,
                    onValueChange = onWorkDateChange,
                    label = { Text(text = stringResource(R.string.timesheet_work_date)) },
                    supportingText = { Text(text = stringResource(R.string.timesheet_date_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = formState.workerName,
                    onValueChange = onWorkerNameChange,
                    label = { Text(text = stringResource(R.string.timesheet_worker_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = formState.startTime,
                        onValueChange = onStartTimeChange,
                        label = { Text(text = stringResource(R.string.timesheet_start_time)) },
                        supportingText = { Text(text = stringResource(R.string.timesheet_time_hint)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = formState.finishTime,
                        onValueChange = onFinishTimeChange,
                        label = { Text(text = stringResource(R.string.timesheet_finish_time)) },
                        supportingText = { Text(text = stringResource(R.string.timesheet_time_hint)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stringResource(R.string.timesheet_hours_worked))
                        Text(
                            text = formState.calculatedHours?.let(WorkEntryTimeUtils::formatHours) ?: "--",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                formState.errorMessage?.let { errorMessage ->
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (formState.entryId != null) {
                        TextButton(onClick = onDelete) {
                            Text(text = stringResource(R.string.timesheet_delete))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    TextButton(onClick = onDismiss) {
                        Text(text = stringResource(R.string.timesheet_cancel))
                    }
                    TextButton(onClick = onSave) {
                        Text(text = stringResource(R.string.timesheet_save))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TimesheetScreenPreview() {
    ProPaintersPlasterersPaymentTheme {
        TimesheetScreen(
            uiState = TimesheetUiState(
                job = JobEntity(
                    jobId = 1,
                    propertyAddress = "12 King Street, Sydney",
                    jobName = "Interior repaint"
                ),
                entries = listOf(
                    WorkEntryEntity(
                        entryId = 1,
                        jobOwnerId = 1,
                        workDate = "2026-04-08",
                        workerName = "Trung",
                        startTime = "08:00",
                        finishTime = "16:30",
                        hoursWorked = 8.5
                    ),
                    WorkEntryEntity(
                        entryId = 2,
                        jobOwnerId = 1,
                        workDate = "2026-04-08",
                        workerName = "Alex",
                        startTime = "09:00",
                        finishTime = "15:00",
                        hoursWorked = 6.0
                    )
                ),
                totalHours = 14.5
            ),
            onAddEntry = {},
            onExportPdf = {},
            onEditEntry = {},
            onDismissForm = {},
            onWorkDateChange = {},
            onWorkerNameChange = {},
            onStartTimeChange = {},
            onFinishTimeChange = {},
            onSaveEntry = {},
            onDeleteEntry = {},
            onMessageShown = {}
        )
    }
}


