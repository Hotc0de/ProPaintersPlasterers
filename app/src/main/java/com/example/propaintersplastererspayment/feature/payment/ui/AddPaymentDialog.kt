package com.example.propaintersplastererspayment.feature.payment.ui

/*
Unused legacy payment dialog (replaced by PaymentScreen/AddEditClientDebtDialog and
ClientPaymentDetailScreen/AddClientPaymentDialog).
Kept commented for safe rollback.

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.propaintersplastererspayment.data.local.entity.ClientEntity
import com.example.propaintersplastererspayment.data.local.entity.PaymentEntity
import com.example.propaintersplastererspayment.ui.theme.IndustrialGold
import com.example.propaintersplastererspayment.ui.theme.OffWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPaymentDialog(
    clients: List<ClientEntity>,
    paymentToEdit: PaymentEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (clientId: Long?, newClientName: String?, amount: Double, notes: String) -> Unit,
    onDelete: (PaymentEntity) -> Unit = {}
) {
    var amount by remember { mutableStateOf(paymentToEdit?.amount?.toString() ?: "") }
    var notes by remember { mutableStateOf(paymentToEdit?.notes ?: "") }

    val initialClient = clients.find { it.clientId == paymentToEdit?.clientId }
    var clientSearch by remember { mutableStateOf(initialClient?.name ?: "") }
    var selectedClient by remember { mutableStateOf<ClientEntity?>(initialClient) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val filteredClients = if (clientSearch.isEmpty()) {
        clients
    } else {
        clients.filter { it.name.contains(clientSearch, ignoreCase = true) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = Color(0xFF1A1A1B),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    if (paymentToEdit == null) "ADD PAYMENT" else "EDIT PAYMENT",
                    color = IndustrialGold,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )

                // Client Selection
                Box {
                    OutlinedTextField(
                        value = clientSearch,
                        onValueChange = {
                            clientSearch = it
                            selectedClient = clients.find { c -> c.name.equals(it, ignoreCase = true) }
                            isDropdownExpanded = true
                        },
                        label = { Text("Client Name", color = OffWhite.copy(alpha = 0.7f)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = OffWhite,
                            unfocusedTextColor = OffWhite,
                            focusedBorderColor = IndustrialGold,
                            unfocusedBorderColor = OffWhite.copy(alpha = 0.5f)
                        ),
                        singleLine = true,
                        readOnly = paymentToEdit != null // Keep client fixed on edit to avoid complexity for now
                    )

                    if (isDropdownExpanded && paymentToEdit == null) {
                        val displayItems = filteredClients.take(5)
                        if (displayItems.isNotEmpty() || (clientSearch.isNotEmpty() && !filteredClients.any { it.name.equals(clientSearch, true) })) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 64.dp)
                                    .heightIn(max = 200.dp),
                                color = Color(0xFF2A2A2B),
                                tonalElevation = 8.dp,
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Column {
                                    displayItems.forEach { client ->
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
                                    if (clientSearch.isNotEmpty() && !filteredClients.any { it.name.equals(clientSearch, true) }) {
                                        ListItem(
                                            headlineContent = { Text("Add new: \"$clientSearch\"", color = IndustrialGold) },
                                            modifier = Modifier.clickable {
                                                isDropdownExpanded = false
                                            },
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
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) amount = it },
                    label = { Text("Total Amount", color = OffWhite.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OffWhite,
                        unfocusedTextColor = OffWhite,
                        focusedBorderColor = IndustrialGold,
                        unfocusedBorderColor = OffWhite.copy(alpha = 0.5f)
                    ),
                    prefix = { Text("$", color = OffWhite) }
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)", color = OffWhite.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OffWhite,
                        unfocusedTextColor = OffWhite,
                        focusedBorderColor = IndustrialGold,
                        unfocusedBorderColor = OffWhite.copy(alpha = 0.5f)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (paymentToEdit != null) {
                        IconButton(onClick = { onDelete(paymentToEdit) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f))
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("CANCEL", color = OffWhite.copy(alpha = 0.7f))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                val finalAmount = amount.toDoubleOrNull() ?: 0.0
                                if (selectedClient != null) {
                                    onConfirm(selectedClient!!.clientId, null, finalAmount, notes)
                                } else if (clientSearch.isNotEmpty()) {
                                    onConfirm(null, clientSearch, finalAmount, notes)
                                }
                            },
                            enabled = (selectedClient != null || clientSearch.isNotEmpty()) && amount.isNotEmpty()
                        ) {
                            Text(if (paymentToEdit == null) "ADD" else "UPDATE", color = IndustrialGold, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
*/
