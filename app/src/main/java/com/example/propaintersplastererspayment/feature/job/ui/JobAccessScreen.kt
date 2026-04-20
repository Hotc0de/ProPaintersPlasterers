package com.example.propaintersplastererspayment.feature.job.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.propaintersplastererspayment.ProPaintersApplication
import com.example.propaintersplastererspayment.R
import com.example.propaintersplastererspayment.data.local.entity.AccessItemEntity
import com.example.propaintersplastererspayment.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun JobAccessRoute(jobId: Long) {
    val application = LocalContext.current.applicationContext as ProPaintersApplication
    val accessItems by application.container.accessRepository.observeAccessItems(jobId).collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<AccessItemEntity?>(null) }

    Scaffold(
        containerColor = CharcoalBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = IndustrialGold,
                contentColor = CharcoalBackground
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Access Detail")
            }
        }
    ) { padding ->
        JobAccessScreen(
            accessItems = accessItems,
            onDelete = { item ->
                scope.launch {
                    application.container.accessRepository.deleteAccessItem(item)
                }
            },
            onEdit = { item -> itemToEdit = item },
            modifier = Modifier.padding(padding)
        )

        if (showAddDialog) {
            AccessItemDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { type, code, instructions ->
                    scope.launch {
                        application.container.accessRepository.saveAccessItem(
                            AccessItemEntity(jobId = jobId, type = type, code = code, instructions = instructions)
                        )
                    }
                    showAddDialog = false
                }
            )
        }

        itemToEdit?.let { item ->
            AccessItemDialog(
                item = item,
                onDismiss = { itemToEdit = null },
                onConfirm = { type, code, instructions ->
                    scope.launch {
                        application.container.accessRepository.saveAccessItem(
                            item.copy(type = type, code = code, instructions = instructions)
                        )
                    }
                    itemToEdit = null
                }
            )
        }
    }
}

@Composable
fun JobAccessScreen(
    accessItems: List<AccessItemEntity>,
    onDelete: (AccessItemEntity) -> Unit,
    onEdit: (AccessItemEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (accessItems.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "No access details added yet.", color = TextMuted)
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(accessItems, key = { it.accessId }) { item ->
                AccessItemCard(item, onDelete = { onDelete(item) }, onEdit = { onEdit(item) })
            }
        }
    }
}

@Composable
fun AccessItemCard(
    item: AccessItemEntity,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CharcoalCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val icon = when (item.type) {
                        "Alarm Code" -> Icons.Default.Notifications
                        "Lockbox" -> Icons.Default.Lock
                        "Key Location" -> Icons.Default.LocationOn
                        else -> Icons.Default.MoreHoriz
                    }
                    Icon(icon, contentDescription = null, tint = IndustrialGold, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.type.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = IndustrialGold,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = IndustrialGold, modifier = Modifier.size(20.dp))
                    }
                    var showConfirm by remember { mutableStateOf(false) }
                    IconButton(onClick = { showConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed, modifier = Modifier.size(20.dp))
                    }
                    if (showConfirm) {
                        com.example.propaintersplastererspayment.ui.components.ConfirmDeleteDialog(
                            title = "Delete Access Item",
                            message = "Are you sure you want to delete this access item?",
                            onConfirm = {
                                showConfirm = false
                                onDelete()
                            },
                            onDismiss = { showConfirm = false }
                        )
                    }
                }
            }
            
            Text(
                text = item.code,
                style = MaterialTheme.typography.headlineSmall,
                color = OffWhite,
                fontWeight = FontWeight.Black
            )
            
            if (item.instructions.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = IndustrialGold.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "INSTRUCTIONS:",
                    style = MaterialTheme.typography.labelSmall,
                    color = IndustrialGold.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.instructions,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
fun AccessItemDialog(
    item: AccessItemEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (type: String, code: String, instructions: String) -> Unit
) {
    var type by remember { mutableStateOf(item?.type ?: "Alarm Code") }
    var code by remember { mutableStateOf(item?.code ?: "") }
    var instructions by remember { mutableStateOf(item?.instructions ?: "") }

    val types = listOf("Alarm Code", "Lockbox", "Key Location", "Other")
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = if (item == null) stringResource(R.string.job_access_add_title) else stringResource(R.string.job_access_edit_title),
                color = IndustrialGold
            ) 
        },
        containerColor = CharcoalCard,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Type Selector
                Box {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.job_access_type), color = IndustrialGold) },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { expanded = true }) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = IndustrialGold)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = OffWhite,
                            unfocusedTextColor = OffWhite,
                            focusedBorderColor = IndustrialGold,
                            unfocusedBorderColor = IndustrialGold.copy(alpha = 0.5f)
                        )
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(CharcoalCard)
                    ) {
                        types.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t, color = OffWhite) },
                                onClick = {
                                    type = t
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text(stringResource(R.string.job_access_code), color = IndustrialGold) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OffWhite,
                        unfocusedTextColor = OffWhite,
                        focusedBorderColor = IndustrialGold,
                        unfocusedBorderColor = IndustrialGold.copy(alpha = 0.5f)
                    )
                )

                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text(stringResource(R.string.job_access_instructions), color = IndustrialGold) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OffWhite,
                        unfocusedTextColor = OffWhite,
                        focusedBorderColor = IndustrialGold,
                        unfocusedBorderColor = IndustrialGold.copy(alpha = 0.5f)
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (code.isNotBlank()) onConfirm(type, code, instructions) },
                enabled = code.isNotBlank()
            ) {
                Text("Save", color = IndustrialGold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}
