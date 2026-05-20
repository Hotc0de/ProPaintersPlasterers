package com.example.propaintersplastererspayment.feature.payment.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.propaintersplastererspayment.ProPaintersApplication
import com.example.propaintersplastererspayment.data.local.entity.PaymentEntity
import com.example.propaintersplastererspayment.feature.payment.vm.PaymentViewModel
import com.example.propaintersplastererspayment.ui.components.IndustrialCard
import com.example.propaintersplastererspayment.ui.theme.IndustrialGold
import com.example.propaintersplastererspayment.ui.theme.OffWhite
import java.text.SimpleDateFormat
import java.util.*

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
    val clientPayments = uiState.payments.filter { it.payment.clientId == clientId }

    var showEditDialog by remember { mutableStateOf(false) }
    var paymentToEdit by remember { mutableStateOf<PaymentEntity?>(null) }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

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
        }
    ) { padding ->
        if (clientPayments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No payments found for this client", color = OffWhite.copy(alpha = 0.5f))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    val totalPaid = clientPayments.sumOf { it.payment.amount }
                    IndustrialCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Total Paid", color = OffWhite.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall)
                            Text(
                                "$${String.format(Locale.getDefault(), "%.2f", totalPaid)}",
                                style = MaterialTheme.typography.headlineMedium,
                                color = IndustrialGold,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "PAYMENT HISTORY",
                        style = MaterialTheme.typography.labelLarge,
                        color = OffWhite.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(clientPayments) { item ->
                    IndustrialCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        dateFormat.format(Date(item.payment.date)),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = OffWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (item.payment.notes.isNotEmpty()) {
                                        Text(
                                            item.payment.notes,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = OffWhite.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "$${String.format(Locale.getDefault(), "%.2f", item.payment.amount)}",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = IndustrialGold,
                                        fontWeight = FontWeight.Black
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = {
                                            paymentToEdit = item.payment
                                            showEditDialog = true
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Edit Payment",
                                            tint = IndustrialGold,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = {
                                            viewModel.deletePayment(item.payment)
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete Payment",
                                            tint = Color(0xFFCF6679),
                                            modifier = Modifier.size(18.dp)
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

    if (showEditDialog) {
        AddEditPaymentDialog(
            clients = clients,
            paymentToEdit = paymentToEdit,
            onDismiss = {
                showEditDialog = false
                paymentToEdit = null
            },
            onConfirm = { _, _, amount, notes ->
                if (paymentToEdit != null) {
                    viewModel.updatePayment(paymentToEdit!!.copy(amount = amount, notes = notes))
                }
                showEditDialog = false
                paymentToEdit = null
            },
            onDelete = { payment ->
                viewModel.deletePayment(payment)
                showEditDialog = false
                paymentToEdit = null
            }
        )
    }
}
