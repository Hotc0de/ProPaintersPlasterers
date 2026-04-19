package com.example.propaintersplastererspayment.feature.home.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.propaintersplastererspayment.R
import com.example.propaintersplastererspayment.core.util.DateFormatUtils
import com.example.propaintersplastererspayment.data.local.entity.JobStatus
import com.example.propaintersplastererspayment.data.local.model.JobWithInvoices
import com.example.propaintersplastererspayment.feature.home.vm.HomeUiState
import com.example.propaintersplastererspayment.ui.components.IndustrialDatePickerDialog
import com.example.propaintersplastererspayment.ui.theme.*
import java.util.*

@Composable
fun JobListRoute(
    onNavigateToCreateJob: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onOpenJob: (Long) -> Unit,
    onNavigateToClients: () -> Unit,
    onNavigateToInvoices: () -> Unit,
    modifier: Modifier = Modifier
) {
    // This is a placeholder for the actual implementation that should use a ViewModel
    // For now, it just shows a UI
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobListScreen(
    uiState: HomeUiState,
    onSearchQueryChange: (TextFieldValue) -> Unit,
    onUpdateJobDates: (Long, Long?, Long?) -> Unit,
    onDeleteJob: (com.example.propaintersplastererspayment.data.local.entity.JobEntity) -> Unit,
    onNavigateToCreateJob: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onOpenJob: (Long) -> Unit,
    onNavigateToClients: () -> Unit,
    onNavigateToInvoices: () -> Unit,
    modifier: Modifier = Modifier
) {
    var jobToDelete by remember { mutableStateOf<com.example.propaintersplastererspayment.data.local.model.JobWithInvoices?>(null) }

    if (jobToDelete != null) {
        AlertDialog(
            onDismissRequest = { jobToDelete = null },
            title = { Text("Delete Job?", color = IndustrialGold) },
            text = { 
                Text(
                    "Are you sure you want to delete \"${jobToDelete?.job?.jobName}\"? This action cannot be undone.",
                    color = OffWhite
                ) 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        jobToDelete?.let { onDeleteJob(it.job) }
                        jobToDelete = null
                    }
                ) {
                    Text("DELETE", color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { jobToDelete = null }) {
                    Text("CANCEL", color = OffWhite)
                }
            },
            containerColor = CharcoalCard
        )
    }

    Scaffold(
        containerColor = CharcoalBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateJob,
                containerColor = IndustrialGold,
                contentColor = CharcoalBackground,
                shape = MaterialTheme.shapes.large
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Job")
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search jobs or clients...", color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IndustrialGold,
                    unfocusedBorderColor = BorderColor,
                    focusedContainerColor = CharcoalCard,
                    unfocusedContainerColor = CharcoalCard,
                    focusedTextColor = OffWhite,
                    unfocusedTextColor = OffWhite
                ),
                shape = MaterialTheme.shapes.medium,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

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
                                onClick = { onOpenJob(jobWithInvoices.job.jobId) },
                                onDeleteClick = { jobToDelete = jobWithInvoices },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun JobCard(
    jobWithInvoices: JobWithInvoices,
    onUpdateDates: (Long?, Long?) -> Unit,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
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
            onDateSelected = { selected -> 
                onUpdateDates(selected, job.finishDateOverride)
                showStartDatePicker = false 
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showFinishDatePicker) {
        IndustrialDatePickerDialog(
            initialTimestamp = finishDate ?: System.currentTimeMillis(),
            onDateSelected = { selected -> 
                onUpdateDates(job.startDateOverride, selected)
                showFinishDatePicker = false 
            },
            onDismiss = { showFinishDatePicker = false }
        )
    }

    // Adaptive font size based on screen width
    val configuration = LocalConfiguration.current
    val fontSize = if (configuration.screenWidthDp <= 360) 8.sp else 13.sp

    Surface(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures(
                onTap = { onClick() },
                onLongPress = { onDeleteClick() }
            )
        },
        color = CharcoalCard,
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = job.jobName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = IndustrialGold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = job.clientNameSnapshot,
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
                                    .size(24.dp)
                                    .background(indicatorColor.copy(alpha = 0.1f), CircleShape)
                                    .border(1.dp, indicatorColor.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = daysLeft.coerceAtLeast(0).toString(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
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
}

@androidx.compose.ui.tooling.preview.Preview(name = "Folded Fold5", widthDp = 320)
@androidx.compose.ui.tooling.preview.Preview(name = "Normal Phone", widthDp = 411)
@Composable
fun JobCardPreview() {
    val sampleJob = JobWithInvoices(
        job = com.example.propaintersplastererspayment.data.local.entity.JobEntity(
            jobId = 1,
            jobName = "Maria Ozawa Project",
            clientNameSnapshot = "Maria Ozawa",
            propertyAddress = "123 Tall & Narrow St",
            status = com.example.propaintersplastererspayment.data.local.entity.JobStatus.WORKING,
            finishDateOverride = System.currentTimeMillis() + (5 * 24 * 60 * 60 * 1000) // 5 days left
        ),
        invoices = emptyList(),
        workEntries = emptyList()
    )
    ProPaintersTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            JobCard(
                jobWithInvoices = sampleJob,
                onUpdateDates = { _, _ -> },
                onClick = {},
                onDeleteClick = {}
            )
        }
    }
}
