package com.example.propaintersplastererspayment.feature.payment.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.propaintersplastererspayment.ProPaintersApplication
import com.example.propaintersplastererspayment.data.local.entity.PaymentEntity
import com.example.propaintersplastererspayment.feature.payment.vm.PaymentViewModel
import com.example.propaintersplastererspayment.ui.components.IndustrialCard
import com.example.propaintersplastererspayment.ui.components.IndustrialDatePickerDialog
import com.example.propaintersplastererspayment.ui.components.IndustrialFAB
import com.example.propaintersplastererspayment.ui.theme.IndustrialGold
import com.example.propaintersplastererspayment.ui.theme.OffWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientPaymentDetailScreen(
    clientId: Long,
    onBack: () -> Unit,
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
    val client = clients.find { it.clientId == clientId }
    val clientPayments = uiState.payments
        .filter { it.payment.clientId == clientId }
        .map { it.payment }
    val totalPaid = clientPayments.sumOf { it.amount }
    val outstanding = (client?.manualTotalDebt ?: 0.0) - totalPaid
    var showPaymentDialog by remember { mutableStateOf(false) }
    var paymentToEdit by remember { mutableStateOf<PaymentEntity?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF0D0D0D),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        client?.name?.uppercase() ?: "CLIENT PAYMENTS",
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
            IndustrialFAB(
                onClick = { showPaymentDialog = true },
                modifier = Modifier.padding(end = 12.dp, bottom = 12.dp)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            IndustrialCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Outstanding", color = OffWhite.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall)
                    Text(
                        formatCurrency(outstanding),
                        style = MaterialTheme.typography.headlineMedium,
                        color = IndustrialGold,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        "Total: ${formatCurrency(client?.manualTotalDebt ?: 0.0)}  Paid: ${formatCurrency(totalPaid)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = OffWhite.copy(alpha = 0.62f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "PAYMENT HISTORY",
                style = MaterialTheme.typography.labelLarge,
                color = OffWhite.copy(alpha = 0.72f),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (clientPayments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.TopStart
                ) {
                    Text(
                        "No payments recorded yet",
                        color = OffWhite.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 112.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(clientPayments, key = { it.paymentId }) { payment ->
                        ClientPaymentCard(
                            payment = payment,
                            onEdit = {
                                paymentToEdit = payment
                                showPaymentDialog = true
                            },
                            onDelete = { viewModel.deletePayment(payment) }
                        )
                    }
                }
            }
        }
    }

    if (showPaymentDialog) {
        AddClientPaymentDialog(
            paymentToEdit = paymentToEdit,
            onDismiss = {
                showPaymentDialog = false
                paymentToEdit = null
            },
            onConfirm = { reference, amount, date, details ->
                if (paymentToEdit != null) {
                    viewModel.updatePayment(
                        paymentToEdit!!.copy(
                            amount = amount,
                            date = date,
                            reference = reference.trim(),
                            details = details.trim(),
                            notes = details.trim()
                        )
                    )
                } else {
                    viewModel.addPayment(
                        clientId = clientId,
                        amount = amount,
                        notes = details.trim(),
                        date = date,
                        reference = reference.trim(),
                        details = details.trim()
                    )
                }
                showPaymentDialog = false
                paymentToEdit = null
            },
            onDelete = {
                paymentToEdit?.let { viewModel.deletePayment(it) }
                showPaymentDialog = false
                paymentToEdit = null
            }
        )
    }
}

@Composable
private fun ClientPaymentCard(
    payment: PaymentEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val details = payment.details.ifBlank { payment.notes }
    val referenceText = payment.reference.ifBlank { "None" }
    val detailsText = if (details.isNotBlank()) "Available" else "None"

    IndustrialCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    dateFormat.format(Date(payment.date)),
                    style = MaterialTheme.typography.titleMedium,
                    color = OffWhite,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = OffWhite.copy(alpha = 0.65f))) {
                            append("Ref: ")
                        }
                        withStyle(SpanStyle(color = IndustrialGold, fontWeight = FontWeight.Bold)) {
                            append(referenceText)
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "Details: $detailsText",
                    style = MaterialTheme.typography.bodySmall,
                    color = OffWhite.copy(alpha = 0.65f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    formatCurrency(payment.amount),
                    style = MaterialTheme.typography.titleLarge,
                    color = IndustrialGold,
                    fontWeight = FontWeight.Black,
                    maxLines = 1
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(30.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit payment",
                            tint = IndustrialGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete payment",
                            tint = Color(0xFFCF6679),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddClientPaymentDialog(
    paymentToEdit: PaymentEntity?,
    onDismiss: () -> Unit,
    onConfirm: (reference: String, amount: Double, date: Long, details: String) -> Unit,
    onDelete: () -> Unit
) {
    var reference by remember(paymentToEdit) { mutableStateOf(paymentToEdit?.reference ?: "") }
    var amount by remember(paymentToEdit) {
        mutableStateOf(paymentToEdit?.amount?.takeIf { it > 0.0 }?.toString() ?: "")
    }
    var details by remember(paymentToEdit) {
        mutableStateOf(paymentToEdit?.details?.ifBlank { paymentToEdit.notes } ?: "")
    }
    var selectedDate by remember(paymentToEdit) {
        mutableLongStateOf(paymentToEdit?.date ?: System.currentTimeMillis())
    }
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val amountValue = amount.toDoubleOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1B),
        title = {
            Text(
                text = if (paymentToEdit == null) "ADD PAYMENT" else "EDIT PAYMENT",
                color = IndustrialGold,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = reference,
                    onValueChange = { reference = it },
                    label = { Text("Reference") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = paymentTextFieldColors()
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { value ->
                        if (value.isEmpty() || value.toDoubleOrNull() != null) {
                            amount = value
                        }
                    },
                    label = { Text("Paid amount") },
                    singleLine = true,
                    prefix = { Text("$", color = OffWhite) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = paymentTextFieldColors()
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = dateFormat.format(Date(selectedDate)),
                        onValueChange = {},
                        label = { Text("Date") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = paymentTextFieldColors()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showDatePicker = true }
                    )
                }
                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("Details") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    colors = paymentTextFieldColors()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    amountValue?.let {
                        onConfirm(reference, it, selectedDate, details)
                    }
                },
                enabled = amountValue != null && amountValue > 0.0
            ) {
                Text(if (paymentToEdit == null) "ADD" else "UPDATE", color = IndustrialGold, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (paymentToEdit != null) {
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

    if (showDatePicker) {
        IndustrialDatePickerDialog(
            initialTimestamp = selectedDate,
            onDateSelected = {
                selectedDate = it
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
private fun paymentTextFieldColors(): TextFieldColors =
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
