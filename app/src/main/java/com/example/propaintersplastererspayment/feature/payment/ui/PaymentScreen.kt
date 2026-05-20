package com.example.propaintersplastererspayment.feature.payment.ui

import androidx.compose.foundation.clickable
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
import com.example.propaintersplastererspayment.ui.components.IndustrialFAB
import com.example.propaintersplastererspayment.ui.theme.IndustrialGold
import com.example.propaintersplastererspayment.ui.theme.OffWhite
import java.text.SimpleDateFormat
import java.util.*

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
    var showAddDialog by remember { mutableStateOf(false) }
    var paymentToEdit by remember { mutableStateOf<PaymentEntity?>(null) }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

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
            IndustrialFAB(
                onClick = { showAddDialog = true }
            )
        }
    ) { padding ->
        if (uiState.payments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No payments found", color = OffWhite.copy(alpha = 0.5f))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.payments) { item ->
                    IndustrialCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                item.client?.clientId?.let { onNavigateToClientDetail(it) }
                            }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        item.client?.name ?: "Unknown Client",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = IndustrialGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        dateFormat.format(Date(item.payment.date)),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OffWhite.copy(alpha = 0.6f)
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Text(
                                        "$${String.format(Locale.getDefault(), "%.2f", item.payment.amount)}",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = OffWhite,
                                        fontWeight = FontWeight.Black
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = {
                                            paymentToEdit = item.payment
                                            showAddDialog = true
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
                            if (item.payment.notes.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    item.payment.notes,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = OffWhite.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddEditPaymentDialog(
            clients = clients,
            paymentToEdit = paymentToEdit,
            onDismiss = { 
                showAddDialog = false
                paymentToEdit = null
            },
            onConfirm = { clientId, newClientName, amount, notes ->
                if (paymentToEdit != null) {
                    viewModel.updatePayment(paymentToEdit!!.copy(amount = amount, notes = notes))
                } else {
                    if (clientId != null) {
                        viewModel.addPayment(clientId, amount, notes)
                    } else if (newClientName != null) {
                        viewModel.addNewClientAndPayment(newClientName, amount, notes)
                    }
                }
                showAddDialog = false
                paymentToEdit = null
            },
            onDelete = { payment ->
                viewModel.deletePayment(payment)
                showAddDialog = false
                paymentToEdit = null
            }
        )
    }
}
