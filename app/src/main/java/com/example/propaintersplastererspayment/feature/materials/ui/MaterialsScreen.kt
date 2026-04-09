package com.example.propaintersplastererspayment.feature.materials.ui

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.propaintersplastererspayment.ProPaintersApplication
import com.example.propaintersplastererspayment.R
import com.example.propaintersplastererspayment.core.util.CurrencyFormatUtils
import com.example.propaintersplastererspayment.data.local.entity.JobEntity
import com.example.propaintersplastererspayment.data.local.entity.MaterialItemEntity
import com.example.propaintersplastererspayment.feature.materials.vm.MaterialFormState
import com.example.propaintersplastererspayment.feature.materials.vm.MaterialsUiState
import com.example.propaintersplastererspayment.feature.materials.vm.MaterialsViewModel
import com.example.propaintersplastererspayment.ui.theme.ProPaintersPlasterersPaymentTheme

@Composable
fun MaterialsRoute(
    jobId: Long,
    modifier: Modifier = Modifier
) {
    val application = LocalContext.current.applicationContext as ProPaintersApplication
    val viewModel: MaterialsViewModel = viewModel(
        factory = MaterialsViewModel.provideFactory(
            jobId = jobId,
            jobRepository = application.container.jobRepository,
            materialRepository = application.container.materialRepository
        )
    )
    val uiState by viewModel.uiState.collectAsState()

    MaterialsScreen(
        uiState = uiState,
        modifier = modifier,
        onAddMaterial = viewModel::openAddMaterial,
        onEditMaterial = viewModel::openEditMaterial,
        onDismissForm = viewModel::dismissForm,
        onMaterialNameChange = viewModel::onMaterialNameChange,
        onPriceChange = viewModel::onPriceChange,
        onSaveMaterial = viewModel::saveMaterial,
        onDeleteMaterial = viewModel::deleteMaterial,
        onMessageShown = viewModel::clearUserMessage
    )
}

@Composable
fun MaterialsScreen(
    uiState: MaterialsUiState,
    onAddMaterial: () -> Unit,
    onEditMaterial: (MaterialItemEntity) -> Unit,
    onDismissForm: () -> Unit,
    onMaterialNameChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onSaveMaterial: () -> Unit,
    onDeleteMaterial: (Long) -> Unit,
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
                ExtendedFloatingActionButton(onClick = onAddMaterial) {
                    Text(text = stringResource(R.string.materials_add_item))
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
                        text = stringResource(R.string.materials_no_job),
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
                        MaterialJobSummaryCard(job = uiState.job)
                    }
                    item {
                        TotalMaterialCostCard(totalMaterialCost = uiState.totalMaterialCost)
                    }
                    if (uiState.materials.isEmpty()) {
                        item {
                            EmptyMaterialsCard()
                        }
                    } else {
                        items(uiState.materials, key = { material -> material.materialId }) { material ->
                            MaterialItemCard(
                                item = material,
                                onEditMaterial = { onEditMaterial(material) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (uiState.isFormVisible) {
        MaterialFormDialog(
            formState = uiState.formState,
            onDismiss = onDismissForm,
            onMaterialNameChange = onMaterialNameChange,
            onPriceChange = onPriceChange,
            onSave = onSaveMaterial,
            onDelete = {
                uiState.formState.materialId?.let(onDeleteMaterial)
            }
        )
    }
}

@Composable
private fun MaterialJobSummaryCard(job: JobEntity) {
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
                    job.jobName.ifBlank { stringResource(R.string.materials_unknown_job) }
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${stringResource(R.string.materials_job_address)}: ${job.propertyAddress}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun TotalMaterialCostCard(totalMaterialCost: Double) {
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
                text = stringResource(R.string.materials_total_cost),
                style = MaterialTheme.typography.titleMedium
            )
            AssistChip(
                onClick = {},
                label = {
                    Text(text = CurrencyFormatUtils.formatCurrency(totalMaterialCost))
                }
            )
        }
    }
}

@Composable
private fun EmptyMaterialsCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.materials_no_items),
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun MaterialItemCard(
    item: MaterialItemEntity,
    onEditMaterial: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onEditMaterial)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = item.materialName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = CurrencyFormatUtils.formatCurrency(item.price),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            TextButton(onClick = onEditMaterial) {
                Text(text = stringResource(R.string.materials_edit))
            }
        }
    }
}

@Composable
fun MaterialFormDialog(
    formState: MaterialFormState,
    onDismiss: () -> Unit,
    onMaterialNameChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
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
                Text(
                    text = if (formState.materialId == null) {
                        stringResource(R.string.materials_add_item)
                    } else {
                        stringResource(R.string.materials_edit_item)
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = formState.materialName,
                    onValueChange = onMaterialNameChange,
                    label = { Text(text = stringResource(R.string.materials_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = formState.priceText,
                    onValueChange = onPriceChange,
                    label = { Text(text = stringResource(R.string.materials_price)) },
                    supportingText = { Text(text = stringResource(R.string.materials_price_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stringResource(R.string.materials_price_preview))
                        Text(
                            text = formState.parsedPrice?.let(CurrencyFormatUtils::formatCurrency) ?: "--",
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
                    if (formState.materialId != null) {
                        TextButton(onClick = onDelete) {
                            Text(text = stringResource(R.string.materials_delete))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    TextButton(onClick = onDismiss) {
                        Text(text = stringResource(R.string.materials_cancel))
                    }
                    TextButton(onClick = onSave) {
                        Text(text = stringResource(R.string.materials_save))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MaterialsScreenPreview() {
    ProPaintersPlasterersPaymentTheme {
        MaterialsScreen(
            uiState = MaterialsUiState(
                job = JobEntity(
                    jobId = 1,
                    propertyAddress = "12 King Street, Sydney",
                    jobName = "Interior repaint"
                ),
                materials = listOf(
                    MaterialItemEntity(
                        materialId = 1,
                        jobOwnerId = 1,
                        materialName = "Paint",
                        price = 240.0
                    ),
                    MaterialItemEntity(
                        materialId = 2,
                        jobOwnerId = 1,
                        materialName = "Plaster",
                        price = 89.95
                    )
                ),
                totalMaterialCost = 329.95
            ),
            onAddMaterial = {},
            onEditMaterial = {},
            onDismissForm = {},
            onMaterialNameChange = {},
            onPriceChange = {},
            onSaveMaterial = {},
            onDeleteMaterial = {},
            onMessageShown = {}
        )
    }
}

