package com.example.propaintersplastererspayment.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.sp
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

    val startDateDisplay = startDate?.let { DateFormatUtils.formatTimestampToDisplay(it) } ?: "Set Start"
    val finishDateDisplay = finishDate?.let { DateFormatUtils.formatTimestampToDisplay(it) } ?: "Set Finish"

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

    val configuration = LocalConfiguration.current
    val fontSize = if (configuration.screenWidthDp <= 360) 8.sp else 13.sp

    IndustrialCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = job.jobName.ifBlank { "Unnamed Project" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = IndustrialGold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = job.clientName.ifBlank { "No Client" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = OffWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = TextMuted
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = job.propertyAddress,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Status badge and Invoice
            val (statusText, statusColor, statusDesc) = when (job.status) {
                JobStatus.WORKING -> Triple("WORKING", SuccessGreen, "Job is currently in progress")
                JobStatus.WAITING_FOR_PAYMENT -> Triple("WAITING", ErrorRed, "Invoice sent, awaiting payment")
                JobStatus.PAID -> Triple("PAID", Color(0xFF42A5F5), "Payment received and job completed")
            }
            var showStatusTooltip by remember { mutableStateOf(false) }

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    shape = MaterialTheme.shapes.extraSmall,
                    color = statusColor.copy(alpha = 0.1f),
                    modifier = Modifier.clickable { showStatusTooltip = true }
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
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
                
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Inv: $invoiceNumber",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Start Date Column
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Date Start",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .clickable { showStartDatePicker = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = IndustrialGold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = startDateDisplay,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = fontSize),
                            fontWeight = FontWeight.Medium,
                            color = OffWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Finish Date Column
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Date Finish",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .clickable { showFinishDatePicker = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = IndustrialGold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = finishDateDisplay,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = fontSize),
                            fontWeight = FontWeight.Medium,
                            color = OffWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Days Left Indicator
                        val daysLeft = finishDate?.let {
                            val diff = it - System.currentTimeMillis()
                            (diff / (1000 * 60 * 60 * 24)).toInt()
                        }

                        if (daysLeft != null && job.status != JobStatus.PAID) {
                            val indicatorColor = when {
                                daysLeft <= 3 -> ErrorRed
                                daysLeft <= 7 -> Color(0xFFFF9800) // Orange
                                else -> SuccessGreen
                            }
                            
                            Spacer(modifier = Modifier.weight(1f))
                            
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(indicatorColor.copy(alpha = 0.1f), CircleShape)
                                    .border(1.dp, indicatorColor.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = daysLeft.coerceAtLeast(0).toString(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 15.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = indicatorColor
                                )
                            }
                        }
                    }
                }
            }
    }
}


