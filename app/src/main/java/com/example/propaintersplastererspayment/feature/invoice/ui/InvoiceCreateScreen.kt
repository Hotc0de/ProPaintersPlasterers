package com.example.propaintersplastererspayment.feature.invoice.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
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
import com.example.propaintersplastererspayment.data.local.entity.ClientEntity
import com.example.propaintersplastererspayment.data.local.entity.JobStatus
import com.example.propaintersplastererspayment.feature.invoice.vm.ClientMode
import com.example.propaintersplastererspayment.feature.invoice.vm.InvoiceCreateUiState
import com.example.propaintersplastererspayment.feature.invoice.vm.InvoiceCreateViewModel
import com.example.propaintersplastererspayment.feature.invoice.vm.QuickInvoiceListViewModel
import com.example.propaintersplastererspayment.ui.components.IndustrialCard
import com.example.propaintersplastererspayment.ui.components.IndustrialTextField
import com.example.propaintersplastererspayment.ui.components.PrimaryButton
import com.example.propaintersplastererspayment.ui.theme.*

@Composable
fun InvoiceCreateRoute(
    onBack: () -> Unit,
    onInvoiceCreated: (Long) -> Unit,
    onOpenQuickInvoice: (Long) -> Unit,
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
        onIncludeDueDateChange = viewModel::onIncludeDueDateChange,
        onDueDateChange = viewModel::onDueDateChange,
        onIncludeGstChange = viewModel::onIncludeGstChange,
        onSave = viewModel::saveInvoice,
        onOpenQuickInvoice = onOpenQuickInvoice,
        onDeleteQuickInvoice = { /* Handled in Tab */ },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceCreateScreen(
    uiState: InvoiceCreateUiState,
    onBack: () -> Unit,
    onClientModeChange: (ClientMode) -> Unit,
    onClientSelected: (ClientEntity) -> Unit,
    onNewClientNameChange: (TextFieldValue) -> Unit,
    onAddressChange: (TextFieldValue) -> Unit,
    onDescriptionChange: (TextFieldValue) -> Unit,
    onQtyChange: (TextFieldValue) -> Unit,
    onRateChange: (TextFieldValue) -> Unit,
    onIncludeDueDateChange: (Boolean) -> Unit,
    onDueDateChange: (TextFieldValue) -> Unit,
    onIncludeGstChange: (Boolean) -> Unit,
    onSave: () -> Unit,
    onOpenQuickInvoice: (Long) -> Unit,
    onDeleteQuickInvoice: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

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
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = CharcoalBackground,
                contentColor = IndustrialGold,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = IndustrialGold
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Create New") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("All Quick Invoices") }
                )
            }

            if (selectedTab == 0) {
                CreateInvoiceTab(
                    uiState = uiState,
                    onClientModeChange = onClientModeChange,
                    onClientSelected = onClientSelected,
                    onNewClientNameChange = onNewClientNameChange,
                    onAddressChange = onAddressChange,
                    onDescriptionChange = onDescriptionChange,
                    onQtyChange = onQtyChange,
                    onRateChange = onRateChange,
                    onIncludeDueDateChange = onIncludeDueDateChange,
                    onDueDateChange = onDueDateChange,
                    onIncludeGstChange = onIncludeGstChange,
                    onSave = onSave
                )
            } else {
                QuickInvoiceListTab(
                    onOpenInvoice = onOpenQuickInvoice
                )
            }
        }
    }
}

@Composable
private fun CreateInvoiceTab(
    uiState: InvoiceCreateUiState,
    onClientModeChange: (ClientMode) -> Unit,
    onClientSelected: (ClientEntity) -> Unit,
    onNewClientNameChange: (TextFieldValue) -> Unit,
    onAddressChange: (TextFieldValue) -> Unit,
    onDescriptionChange: (TextFieldValue) -> Unit,
    onQtyChange: (TextFieldValue) -> Unit,
    onRateChange: (TextFieldValue) -> Unit,
    onIncludeDueDateChange: (Boolean) -> Unit,
    onDueDateChange: (TextFieldValue) -> Unit,
    onIncludeGstChange: (Boolean) -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

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

                if (uiState.clientMode == ClientMode.Existing && uiState.propertyAddresses.isNotEmpty()) {
                    var addressExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        IndustrialTextField(
                            value = uiState.address,
                            onValueChange = onAddressChange,
                            label = "Property Address",
                            placeholder = "Where the work was done",
                            minLines = 2,
                            trailingIcon = {
                                IconButton(onClick = { addressExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, null, tint = IndustrialGold)
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = addressExpanded,
                            onDismissRequest = { addressExpanded = false },
                            modifier = Modifier.background(CharcoalCard).fillMaxWidth(0.9f)
                        ) {
                            uiState.propertyAddresses.forEach { addr ->
                                DropdownMenuItem(
                                    text = { Text(addr, color = OffWhite) },
                                    onClick = {
                                        onAddressChange(TextFieldValue(addr))
                                        addressExpanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    IndustrialTextField(
                        value = uiState.address,
                        onValueChange = onAddressChange,
                        label = "Property Address",
                        placeholder = "Where the work was done",
                        minLines = 2
                    )
                }
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
                    Text("Include Due Date", color = OffWhite)
                    Switch(
                        checked = uiState.includeDueDate,
                        onCheckedChange = onIncludeDueDateChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = IndustrialGold,
                            checkedTrackColor = IndustrialGold.copy(alpha = 0.5f)
                        )
                    )
                }

                if (uiState.includeDueDate) {
                    IndustrialTextField(
                        value = uiState.dueDate,
                        onValueChange = onDueDateChange,
                        label = "Due Date",
                        placeholder = "dd-MM-yyyy"
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

@Composable
private fun QuickInvoiceListTab(
    onOpenInvoice: (Long) -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as ProPaintersApplication
    val viewModel: QuickInvoiceListViewModel = viewModel(
        factory = QuickInvoiceListViewModel.provideFactory(application.container.jobRepository)
    )
    val uiState by viewModel.uiState.collectAsState()
    val invoices = uiState.invoices
    var jobToDelete by remember { mutableStateOf<Long?>(null) }

    if (jobToDelete != null) {
        AlertDialog(
            onDismissRequest = { jobToDelete = null },
            title = { Text("Delete Quick Invoice", color = OffWhite) },
            text = { Text("Are you sure you want to delete this invoice? This action cannot be undone.", color = OffWhite) },
            containerColor = CharcoalCard,
            confirmButton = {
                TextButton(onClick = {
                    jobToDelete?.let { viewModel.deleteQuickInvoice(it) }
                    jobToDelete = null
                }) {
                    Text("Delete", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { jobToDelete = null }) {
                    Text("Cancel", color = IndustrialGold)
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // ── Search Bar ──────────────────────────────────────────────
        PaddingValues(horizontal = 16.dp, vertical = 12.dp).let { padding ->
            IndustrialTextField(
                value = TextFieldValue(uiState.searchQuery),
                onValueChange = { viewModel.onSearchQueryChange(it.text) },
                label = "Search Invoices",
                placeholder = "Search by client, address or #",
                leadingIcon = { Icon(Icons.Default.Search, null, tint = IndustrialGold) },
                trailingIcon = if (uiState.searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Close, null, tint = TextMuted)
                        }
                    }
                } else null,
                modifier = Modifier.padding(16.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (invoices.isEmpty()) {
                Text(
                    text = if (uiState.searchQuery.isEmpty()) "No quick invoices found" else "No matches found",
                    modifier = Modifier.align(Alignment.Center),
                    color = TextMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            } else {
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(invoices, key = { it.job.jobId }) { jobWithInvoices ->
                        val job = jobWithInvoices.job
                        val invoice = jobWithInvoices.invoices.firstOrNull()

                        IndustrialCard(
                            onClick = { onOpenInvoice(job.jobId) }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        job.clientNameSnapshot,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = IndustrialGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        job.propertyAddress,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            "Inv: ${invoice?.invoiceNumber ?: "N/A"}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = OffWhite,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = { jobToDelete = job.jobId },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = ErrorRed.copy(alpha = 0.7f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        CurrencyFormatUtils.formatCurrency(invoice?.totalAmount ?: 0.0),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = IndustrialGold,
                                        fontWeight = FontWeight.Black
                                    )
                                    // Status badge
                                    val (statusText, statusColor) = when (job.status) {
                                        JobStatus.WORKING -> "WORKING" to SuccessGreen
                                        JobStatus.WAITING_FOR_PAYMENT -> "WAITING" to ErrorRed
                                        JobStatus.PAID -> "PAID" to Color(0xFF42A5F5)
                                    }
                                    Surface(
                                        shape = MaterialTheme.shapes.extraSmall,
                                        color = statusColor.copy(alpha = 0.15f)
                                    ) {
                                        @Suppress("DEPRECATION")
                                        Text(
                                            text = statusText,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = statusColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
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
