package com.example.propaintersplastererspayment.feature.home.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.propaintersplastererspayment.ProPaintersApplication
import com.example.propaintersplastererspayment.R
import com.example.propaintersplastererspayment.data.local.entity.JobEntity
import com.example.propaintersplastererspayment.data.local.entity.JobStatus
import com.example.propaintersplastererspayment.data.local.model.JobWithInvoices
import com.example.propaintersplastererspayment.feature.home.vm.HomeUiState
import com.example.propaintersplastererspayment.feature.home.vm.HomeViewModel
import com.example.propaintersplastererspayment.ui.components.*
import com.example.propaintersplastererspayment.ui.theme.*

@Composable
fun HomeRoute(
    onOpenSettings: () -> Unit,
    onAddJob: () -> Unit,
    onOpenJob: (Long) -> Unit,
    onOpenClients: () -> Unit = {},
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
        onOpenSettings = onOpenSettings,
        onAddJob = onAddJob,
        onOpenJob = onOpenJob,
        onOpenClients = onOpenClients,
        modifier = modifier
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onSearchQueryChange: (TextFieldValue) -> Unit,
    onOpenSettings: () -> Unit,
    onAddJob: () -> Unit,
    onOpenJob: (Long) -> Unit,
    onOpenClients: () -> Unit = {},
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
                Text(
                    text = stringResource(R.string.home_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = IndustrialGold,
                    fontWeight = FontWeight.Bold
                )

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
private fun JobCard(jobWithInvoices: JobWithInvoices, onClick: () -> Unit) {
    val job = jobWithInvoices.job
    val invoiceNumber = jobWithInvoices.invoices.firstOrNull()?.invoiceNumber ?: "N/A"

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

            // Status badge
            val statusText = when (job.status) {
                JobStatus.WORKING -> "WORKING"
                JobStatus.WAITING_FOR_PAYMENT -> "WAITING FOR PAYMENT"
                JobStatus.PAID -> "PAID"
            }
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = IndustrialGold.copy(alpha = 0.2f)
            ) {
                Text(
                    text = statusText,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = IndustrialGold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = BorderColor)
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Invoice",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
                Text(
                    text = invoiceNumber,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = IndustrialGold
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Client Name",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
                Text(
                    text = job.clientName.ifBlank { "N/A" },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = IndustrialGold
                )
            }
        }
    }
}
