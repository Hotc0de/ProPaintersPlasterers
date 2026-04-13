package com.example.propaintersplastererspayment.feature.materials.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.propaintersplastererspayment.ProPaintersApplication
import com.example.propaintersplastererspayment.R
import com.example.propaintersplastererspayment.core.util.CurrencyFormatUtils
import com.example.propaintersplastererspayment.data.local.entity.MaterialItemEntity
import com.example.propaintersplastererspayment.feature.materials.vm.MaterialFormState
import com.example.propaintersplastererspayment.feature.materials.vm.MaterialsUiState
import com.example.propaintersplastererspayment.feature.materials.vm.MaterialsViewModel
import com.example.propaintersplastererspayment.ui.components.*
import com.example.propaintersplastererspayment.ui.theme.*

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
    onMaterialNameChange: (TextFieldValue) -> Unit,
    onPriceChange: (TextFieldValue) -> Unit,
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
        containerColor = CharcoalBackground,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            if (uiState.job != null) {
                IndustrialFAB(onClick = onAddMaterial)
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
                            text = stringResource(R.string.materials_no_job),
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextMuted
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            TotalMaterialHeroCard(totalMaterialCost = uiState.totalMaterialCost)
                        }

                        if (uiState.materials.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(R.string.materials_no_items),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSubdued
                                    )
                                }
                            }
                        } else {
                            items(uiState.materials, key = { it.materialId }) { material ->
                                MaterialItemCard(
                                    item = material,
                                    onClick = { onEditMaterial(material) }
                                )
                            }
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
private fun TotalMaterialHeroCard(totalMaterialCost: Double) {
    IndustrialCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = stringResource(R.string.materials_total_cost),
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = CurrencyFormatUtils.formatCurrency(totalMaterialCost),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = IndustrialGold
            )
        }
    }
}

@Composable
private fun MaterialItemCard(item: MaterialItemEntity, onClick: () -> Unit) {
    IndustrialCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = AppShapes.medium,
                    color = IndustrialGold.copy(alpha = 0.1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = IndustrialGold,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = item.materialName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OffWhite
                    )
                    Text(
                        text = "Material", // Or a category if available
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSubdued
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = CurrencyFormatUtils.formatCurrency(item.price),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = IndustrialGold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextSubdued,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun MaterialFormDialog(
    formState: MaterialFormState,
    onDismiss: () -> Unit,
    onMaterialNameChange: (TextFieldValue) -> Unit,
    onPriceChange: (TextFieldValue) -> Unit,
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
                        text = if (formState.materialId == null) "Add Material" else "Edit Material",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = OffWhite
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                IndustrialTextField(
                    value = formState.materialName,
                    onValueChange = onMaterialNameChange,
                    label = stringResource(R.string.materials_name),
                    placeholder = "Material Name"
                )

                IndustrialTextField(
                    value = formState.priceText,
                    onValueChange = onPriceChange,
                    label = stringResource(R.string.materials_price),
                    placeholder = "0.00",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                formState.errorMessage?.let {
                    Text(text = it, color = ErrorRed, style = MaterialTheme.typography.bodySmall)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (formState.materialId != null) {
                        OutlinedButton(
                            onClick = onDelete,
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = AppShapes.large,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f))
                        ) {
                            Text("Delete", fontWeight = FontWeight.Bold)
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
