package com.example.propaintersplastererspayment.feature.home.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.propaintersplastererspayment.ProPaintersApplication
import com.example.propaintersplastererspayment.R
import com.example.propaintersplastererspayment.core.util.DateFormatUtils
import com.example.propaintersplastererspayment.data.local.entity.JobEntity
import com.example.propaintersplastererspayment.data.local.entity.JobStatus
import com.example.propaintersplastererspayment.data.local.model.JobWithInvoices
import com.example.propaintersplastererspayment.feature.home.vm.HomeUiState
import com.example.propaintersplastererspayment.feature.home.vm.HomeViewModel
import com.example.propaintersplastererspayment.ui.components.*
import com.example.propaintersplastererspayment.ui.theme.*
import java.util.Calendar

@Composable
fun HomeRoute(
    onOpenSettings: () -> Unit,
    onAddJob: () -> Unit,
    onOpenJob: (Long) -> Unit,
    onOpenClients: () -> Unit = {},
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val application = LocalContext.current.applicationContext as ProPaintersApplication
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.provideFactory(application.container.jobRepository)
    )
    val uiState by viewModel.uiState.collectAsState()

    HomeScreen(
        uiState = uiState,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onUpdateJobDates = viewModel::updateJobDates,
        onOpenSettings = onOpenSettings,
        onAddJob = onAddJob,
        onOpenJob = onOpenJob,
        onOpenClients = onOpenClients,
        onBack = onBack,
        modifier = modifier
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onSearchQueryChange: (TextFieldValue) -> Unit,
    onUpdateJobDates: (Long, Long?, Long?) -> Unit,
    onOpenSettings: () -> Unit,
    onAddJob: () -> Unit,
    onOpenJob: (Long) -> Unit,
    onOpenClients: () -> Unit = {},
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CharcoalBackground,
        floatingActionButton = {
            IndustrialFAB(
                onClick = onAddJob,
                icon = Icons.Default.Add
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(AppDimensions.screenPadding)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = IndustrialGold
                        )
                    }
                    Text(
                        text = stringResource(R.string.home_title),
                        style = MaterialTheme.typography.headlineLarge,
                        color = IndustrialGold,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconButton(
                        onClick = onOpenClients,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            shape = AppShapes.large,
                            color = CharcoalSecondary
                        ) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = "Clients",
                                tint = OffWhite,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            shape = AppShapes.large,
                            color = CharcoalSecondary
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = OffWhite,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            IndustrialTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                label = "Search",
                placeholder = "Search Invoice, Name or Address...",
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = IndustrialGold
                    )
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )

            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = IndustrialGold)
                    }
                }

                uiState.jobs.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (uiState.searchQuery.text.isBlank()) 
                                stringResource(R.string.home_empty_jobs) 
                            else 
                                "No matching jobs found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextMuted
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 90.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.jobs, key = { it.job.jobId }) { jobWithInvoices ->
                            JobCard(
                                jobWithInvoices = jobWithInvoices,
                                onUpdateDates = { start, finish -> 
                                    onUpdateJobDates(jobWithInvoices.job.jobId, start, finish)
                                },
                                onClick = { onOpenJob(jobWithInvoices.job.jobId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun JobCard(
    jobWithInvoices: JobWithInvoices,
    onUpdateDates: (Long?, Long?) -> Unit,
    onClick: () -> Unit
) {
    val job = jobWithInvoices.job
    val invoiceNumber = jobWithInvoices.invoices.firstOrNull()?.invoiceNumber ?: "N/A"
    
    // Timeline logic
    val startDate = job.startDateOverride ?: jobWithInvoices.workEntries
        .mapNotNull { DateFormatUtils.parseStoredDate(it.workDate)?.time }
        .minOrNull()
    val finishDate = job.finishDateOverride ?: jobWithInvoices.invoices
        .mapNotNull { DateFormatUtils.parseStoredDate(it.invoiceDate)?.time }
        .maxOrNull()

    val startDateDisplay = startDate?.let { DateFormatUtils.formatTimestampToDisplay(it) } ?: "TBA"
    val finishDateDisplay = if (job.status == JobStatus.WORKING && job.finishDateOverride == null) {
        "N/A"
    } else {
        finishDate?.let { DateFormatUtils.formatTimestampToDisplay(it) } ?: "N/A"
    }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showFinishDatePicker by remember { mutableStateOf(false) }

    if (showStartDatePicker) {
        IndustrialDatePickerDialog(
            initialTimestamp = startDate ?: System.currentTimeMillis(),
            onDateSelected = { 
                onUpdateDates(it, job.finishDateOverride)
                showStartDatePicker = false 
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showFinishDatePicker) {
        IndustrialDatePickerDialog(
            initialTimestamp = finishDate ?: System.currentTimeMillis(),
            onDateSelected = { 
                onUpdateDates(job.startDateOverride, it)
                showFinishDatePicker = false 
            },
            onDismiss = { showFinishDatePicker = false }
        )
    }

    IndustrialCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = AppShapes.medium,
                    color = IndustrialGold.copy(alpha = 0.1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Work,
                        contentDescription = null,
                        tint = IndustrialGold,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Column {
                    Text(
                        text = job.clientName.ifBlank { job.jobName.ifBlank { "Unnamed Job" } },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = IndustrialGold
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = TextMuted
                        )
                        Text(
                            text = job.propertyAddress,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            }

            // Status badge and Invoice
            val (statusText, statusColor, statusDesc) = when (job.status) {
                JobStatus.WORKING -> Triple("WORKING", SuccessGreen, "Job is currently in progress")
                JobStatus.WAITING_FOR_PAYMENT -> Triple("WAITING", IndustrialGold, "Invoice sent, awaiting payment")
                JobStatus.PAID -> Triple("PAID", Color(0xFF42A5F5), "Payment received and job completed")
            }
            var showStatusTooltip by remember { mutableStateOf(false) }

            Column(horizontalAlignment = Alignment.End) {
                Box {
                    Surface(
                        shape = MaterialTheme.shapes.extraLarge,
                        color = statusColor.copy(alpha = 0.15f),
                        modifier = Modifier.clickable { showStatusTooltip = true }
                    ) {
                        Text(
                            text = statusText,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }

                    if (showStatusTooltip) {
                        AlertDialog(
                            onDismissRequest = { showStatusTooltip = false },
                            confirmButton = {
                                TextButton(onClick = { showStatusTooltip = false }) {
                                    Text("OK", color = IndustrialGold)
                                }
                            },
                            title = { Text(statusText, color = IndustrialGold) },
                            text = { Text(statusDesc, color = OffWhite) },
                            containerColor = CharcoalCard
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Invoice",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Text(
                        text = invoiceNumber,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = BorderColor)
        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Project Timeline",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = startDateDisplay,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = IndustrialGold,
                    modifier = Modifier.clickable { showStartDatePicker = true }
                )
                Text(
                    text = " — ",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextMuted,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                Text(
                    text = finishDateDisplay,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = IndustrialGold,
                    modifier = Modifier.clickable { showFinishDatePicker = true }
                )
            }
        }
    }
}


