package com.example.propaintersplastererspayment.feature.invoice.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.propaintersplastererspayment.ProPaintersApplication
import com.example.propaintersplastererspayment.core.util.CurrencyFormatUtils
import com.example.propaintersplastererspayment.feature.invoice.vm.ClientMode
import com.example.propaintersplastererspayment.feature.invoice.vm.InvoiceCreateUiState
import com.example.propaintersplastererspayment.feature.invoice.vm.InvoiceCreateViewModel
import com.example.propaintersplastererspayment.ui.components.IndustrialCard
import com.example.propaintersplastererspayment.ui.components.IndustrialTextField
import com.example.propaintersplastererspayment.ui.components.PrimaryButton
import com.example.propaintersplastererspayment.ui.theme.*

@Composable
fun InvoiceCreateRoute(
    onBack: () -> Unit,
    onInvoiceCreated: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val application = LocalContext.current.applicationContext as ProPaintersApplication
    val viewModel: InvoiceCreateViewModel = viewModel(
        factory = InvoiceCreateViewModel.provideFactory(
            clientRepository = application.container.clientRepository,
            jobRepository = application.container.jobRepository,
            invoiceRepository = application.container.invoiceRepository,
            settingsRepository = application.container.settingsRepository
        )
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.successJobId) {
        uiState.successJobId?.let { jobId ->
            onInvoiceCreated(jobId)
        }
    }

    InvoiceCreateScreen(
        uiState = uiState,
        onBack = onBack,
        onClientModeChange = viewModel::onClientModeChange,
        onClientSelected = viewModel::onClientSelected,
        onNewClientNameChange = viewModel::onNewClientNameChange,
        onAddressChange = viewModel::onAddressChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onQtyChange = viewModel::onQtyChange,
        onRateChange = viewModel::onRateChange,
        onIncludeGstChange = viewModel::onIncludeGstChange,
        onSave = viewModel::saveInvoice,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceCreateScreen(
    uiState: InvoiceCreateUiState,
    onBack: () -> Unit,
    onClientModeChange: (ClientMode) -> Unit,
    onClientSelected: (com.example.propaintersplastererspayment.data.local.entity.ClientEntity) -> Unit,
    onNewClientNameChange: (TextFieldValue) -> Unit,
    onAddressChange: (TextFieldValue) -> Unit,
    onDescriptionChange: (TextFieldValue) -> Unit,
    onQtyChange: (TextFieldValue) -> Unit,
    onRateChange: (TextFieldValue) -> Unit,
    onIncludeGstChange: (Boolean) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CharcoalBackground,
        topBar = {
            TopAppBar(
                title = { Text("Quick Invoice", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = IndustrialGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CharcoalBackground,
                    titleContentColor = OffWhite
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ── Client Section ─────────────────────────────────────────────
            SectionHeader(title = "Client Information", icon = Icons.Default.Business)

            IndustrialCard {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    TabRow(
                        selectedTabIndex = if (uiState.clientMode == ClientMode.Existing) 0 else 1,
                        containerColor = Color.Transparent,
                        contentColor = IndustrialGold,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[if (uiState.clientMode == ClientMode.Existing) 0 else 1]),
                                color = IndustrialGold
                            )
                        },
                        divider = {}
                    ) {
                        Tab(
                            selected = uiState.clientMode == ClientMode.Existing,
                            onClick = { onClientModeChange(ClientMode.Existing) },
                            text = { Text("Existing") }
                        )
                        Tab(
                            selected = uiState.clientMode == ClientMode.New,
                            onClick = { onClientModeChange(ClientMode.New) },
                            text = { Text("New Client") }
                        )
                    }

                    if (uiState.clientMode == ClientMode.Existing) {
                        var expanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            IndustrialTextField(
                                value = TextFieldValue(uiState.selectedClient?.name ?: ""),
                                onValueChange = {},
                                label = "Select Client",
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = { expanded = true }) {
                                        Icon(Icons.Default.ArrowDropDown, null, tint = IndustrialGold)
                                    }
                                },
                                modifier = Modifier.clickable { expanded = true }
                            )
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.background(CharcoalCard).fillMaxWidth(0.9f)
                            ) {
                                uiState.existingClients.forEach { client ->
                                    DropdownMenuItem(
                                        text = { Text(client.name, color = OffWhite) },
                                        onClick = {
                                            onClientSelected(client)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        IndustrialTextField(
                            value = uiState.newClientName,
                            onValueChange = onNewClientNameChange,
                            label = "Client Name",
                            placeholder = "Enter name"
                        )
                    }

                    IndustrialTextField(
                        value = uiState.address,
                        onValueChange = onAddressChange,
                        label = "Property Address",
                        placeholder = "Where the work was done",
                        minLines = 2
                    )
                }
            }

            // ── Invoice Details ───────────────────────────────────────────
            SectionHeader(title = "Invoice Details", icon = Icons.Default.Receipt)

            IndustrialCard {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    IndustrialTextField(
                        value = uiState.description,
                        onValueChange = onDescriptionChange,
                        label = "Description",
                        placeholder = "What was done?"
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IndustrialTextField(
                            value = uiState.qty,
                            onValueChange = onQtyChange,
                            label = "Qty",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        IndustrialTextField(
                            value = uiState.rate,
                            onValueChange = onRateChange,
                            label = "Rate ($)",
                            placeholder = "0.00",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1.5f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Include GST (15%)", color = OffWhite)
                        Switch(
                            checked = uiState.includeGst,
                            onCheckedChange = onIncludeGstChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = IndustrialGold,
                                checkedTrackColor = IndustrialGold.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }

            // ── Totals ───────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(IndustrialGold, IndustrialGoldDark)
                        ),
                        shape = AppShapes.large
                    )
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text("TOTAL AMOUNT", style = MaterialTheme.typography.labelLarge, color = CharcoalBackground)
                        Text(
                            text = CurrencyFormatUtils.formatCurrency(uiState.total),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            color = CharcoalBackground
                        )
                    }
                    if (uiState.includeGst) {
                        Text(
                            "Inc. ${CurrencyFormatUtils.formatCurrency(uiState.gstAmount)} GST",
                            style = MaterialTheme.typography.bodySmall,
                            color = CharcoalBackground.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            if (uiState.error != null) {
                Text(uiState.error!!, color = ErrorRed, style = MaterialTheme.typography.bodySmall)
            }

            PrimaryButton(
                text = "Save & Create Invoice",
                onClick = onSave,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                enabled = !uiState.isSaving
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = IndustrialGold, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = IndustrialGold,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}
