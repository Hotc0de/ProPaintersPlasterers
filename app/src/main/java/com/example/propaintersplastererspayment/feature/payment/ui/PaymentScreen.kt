package com.example.propaintersplastererspayment.feature.payment.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.propaintersplastererspayment.ProPaintersApplication
import com.example.propaintersplastererspayment.data.local.entity.ClientEntity
import com.example.propaintersplastererspayment.feature.payment.vm.ClientDebtSummary
import com.example.propaintersplastererspayment.feature.payment.vm.PaymentViewModel
import com.example.propaintersplastererspayment.ui.components.IndustrialCard
import com.example.propaintersplastererspayment.ui.components.IndustrialFAB
import com.example.propaintersplastererspayment.ui.theme.IndustrialGold
import com.example.propaintersplastererspayment.ui.theme.OffWhite
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    onBack: () -> Unit,
    onNavigateToClientDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val application = LocalContext.current.applicationContext as ProPaintersApplication
    val viewModel: PaymentViewModel = viewModel(
        factory = PaymentViewModel.provideFactory(
            application.container.paymentRepository,
            application.container.clientRepository
        )
    )

    val uiState by viewModel.uiState.collectAsState()
    val clients by viewModel.clients.collectAsState()
    var showDebtDialog by remember { mutableStateOf(false) }
    var debtToEdit by remember { mutableStateOf<ClientDebtSummary?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF0D0D0D),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "PAYMENTS",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D0D0D),
                    titleContentColor = IndustrialGold,
                    navigationIconContentColor = IndustrialGold
                )
            )
        },
        floatingActionButton = {
            IndustrialFAB(onClick = { showDebtDialog = true })
        }
    ) { padding ->
        if (uiState.debtClients.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No outstanding client balances", color = OffWhite.copy(alpha = 0.5f))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.debtClients, key = { it.client.clientId }) { debt ->
                    ClientDebtCard(
                        debt = debt,
                        onOpen = { onNavigateToClientDetail(debt.client.clientId) },
                        onEdit = {
                            debtToEdit = debt
                            showDebtDialog = true
                        },
                        onDelete = { viewModel.clearClientDebt(debt.client) }
                    )
                }
            }
        }
    }

    if (showDebtDialog) {
        AddEditClientDebtDialog(
            clients = clients,
            debtToEdit = debtToEdit,
            onDismiss = {
                showDebtDialog = false
                debtToEdit = null
            },
            onConfirm = { clientId, newClientName, totalDebt ->
                if (debtToEdit != null) {
                    viewModel.updateClientDebt(debtToEdit!!.client, totalDebt)
                } else {
                    viewModel.saveClientDebt(clientId, newClientName, totalDebt)
                }
                showDebtDialog = false
                debtToEdit = null
            },
            onDelete = {
                debtToEdit?.let { viewModel.clearClientDebt(it.client) }
                showDebtDialog = false
                debtToEdit = null
            }
        )
    }
}

@Composable
private fun ClientDebtCard(
    debt: ClientDebtSummary,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    IndustrialCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        debt.client.name.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = IndustrialGold,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        "Total: ${formatCurrency(debt.totalDebt)}  Paid: ${formatCurrency(debt.totalPaid)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = OffWhite.copy(alpha = 0.62f)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit client debt",
                            tint = IndustrialGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete client debt",
                            tint = Color(0xFFCF6679),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            Text(
                formatCurrency(debt.outstanding),
                style = MaterialTheme.typography.headlineMedium,
                color = IndustrialGold,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun AddEditClientDebtDialog(
    clients: List<ClientEntity>,
    debtToEdit: ClientDebtSummary?,
    onDismiss: () -> Unit,
    onConfirm: (clientId: Long?, newClientName: String?, totalDebt: Double) -> Unit,
    onDelete: () -> Unit
) {
    var clientSearch by remember(debtToEdit) { mutableStateOf(debtToEdit?.client?.name ?: "") }
    var selectedClient by remember(debtToEdit) { mutableStateOf<ClientEntity?>(debtToEdit?.client) }
    var amount by remember(debtToEdit) {
        mutableStateOf(debtToEdit?.totalDebt?.takeIf { it > 0.0 }?.toString() ?: "")
    }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val clientInteractionSource = remember { MutableInteractionSource() }
    val isClientFocused by clientInteractionSource.collectIsFocusedAsState()
    LaunchedEffect(isClientFocused) {
        if (isClientFocused && debtToEdit == null) {
            isDropdownExpanded = true
        }
    }

    val amountValue = amount.toDoubleOrNull()
    val filteredClients = if (clientSearch.isBlank()) {
        clients
    } else {
        clients.filter { it.name.contains(clientSearch, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1B),
        title = {
            Text(
                if (debtToEdit == null) "ADD CLIENT DEBT" else "EDIT CLIENT DEBT",
                color = IndustrialGold,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
	                Box {
	                    OutlinedTextField(
	                        value = clientSearch,
                        onValueChange = {
                            clientSearch = it
                            selectedClient = clients.find { client ->
                                client.name.equals(it, ignoreCase = true)
                            }
                            isDropdownExpanded = true
                        },
	                        label = { Text("Client") },
	                        readOnly = debtToEdit != null,
	                        singleLine = true,
                            interactionSource = clientInteractionSource,
	                        modifier = Modifier.fillMaxWidth(),
	                        colors = paymentDialogTextFieldColors()
	                    )

                    if (isDropdownExpanded && debtToEdit == null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 64.dp)
                                .heightIn(max = 180.dp),
                            color = Color(0xFF2A2A2B),
                            tonalElevation = 8.dp
	                        ) {
	                            LazyColumn(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
	                                items(filteredClients, key = { it.clientId }) { client ->
	                                    ListItem(
	                                        headlineContent = { Text(client.name, color = OffWhite) },
	                                        modifier = Modifier.clickable {
                                            selectedClient = client
                                            clientSearch = client.name
                                            isDropdownExpanded = false
                                        },
	                                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
	                                    )
	                                }
	                                if (clientSearch.isNotBlank() && filteredClients.none { it.name.equals(clientSearch, true) }) {
                                        item {
                                            ListItem(
                                                headlineContent = { Text("Add new: $clientSearch", color = IndustrialGold) },
                                                modifier = Modifier.clickable { isDropdownExpanded = false },
                                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                                            )
                                        }
	                                }
	                            }
	                        }
	                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { value ->
                        if (value.isEmpty() || value.toDoubleOrNull() != null) {
                            amount = value
                        }
                    },
                    label = { Text("Amount they haven't paid") },
                    singleLine = true,
                    prefix = { Text("$", color = OffWhite) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = paymentDialogTextFieldColors()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    amountValue?.let {
                        onConfirm(selectedClient?.clientId, clientSearch.takeIf { selectedClient == null }, it)
                    }
                },
                enabled = clientSearch.isNotBlank() && amountValue != null && amountValue >= 0.0
            ) {
                Text(if (debtToEdit == null) "ADD" else "UPDATE", color = IndustrialGold, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (debtToEdit != null) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFCF6679))
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("CANCEL", color = OffWhite.copy(alpha = 0.7f))
                }
            }
        }
    )
}

@Composable
private fun paymentDialogTextFieldColors(): TextFieldColors =
    OutlinedTextFieldDefaults.colors(
        focusedTextColor = OffWhite,
        unfocusedTextColor = OffWhite,
        focusedBorderColor = IndustrialGold,
        unfocusedBorderColor = OffWhite.copy(alpha = 0.5f),
        focusedLabelColor = IndustrialGold,
        unfocusedLabelColor = OffWhite.copy(alpha = 0.7f),
        cursorColor = IndustrialGold
    )

private fun formatCurrency(value: Double): String =
    "$${String.format(Locale.getDefault(), "%.2f", value)}"
