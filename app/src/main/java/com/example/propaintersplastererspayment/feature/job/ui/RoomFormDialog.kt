package com.example.propaintersplastererspayment.feature.job.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.propaintersplastererspayment.data.local.entity.RoomEntity
import com.example.propaintersplastererspayment.data.local.entity.RoomType
import com.example.propaintersplastererspayment.ui.theme.CharcoalBackground
import com.example.propaintersplastererspayment.ui.theme.IndustrialGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomFormDialog(
    room: RoomEntity,
    onDismiss: () -> Unit,
    onSave: (RoomEntity) -> Unit
) {
    var customName by remember { mutableStateOf(room.customName) }
    var roomType by remember { mutableStateOf(room.roomType) }
    var notes by remember { mutableStateOf(room.notes ?: "") }
    
    var typeDropdownExpanded by remember { mutableStateOf(false) }
    
    val isNew = room.roomId == 0L
    val canSave = roomType != null

    // Calculate display name based on rules
    val calculatedDisplayName = if (customName.isNotBlank()) customName else roomType.name.lowercase().replaceFirstChar { it.uppercase() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.extraLarge,
            color = CharcoalBackground
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (isNew) "Add Room" else "Edit Room",
                    style = MaterialTheme.typography.headlineSmall,
                    color = IndustrialGold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Room Type Dropdown (REQUIRED)
                ExposedDropdownMenuBox(
                    expanded = typeDropdownExpanded,
                    onExpandedChange = { typeDropdownExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = roomType.name.lowercase().replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Room Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            focusedLabelColor = IndustrialGold,
                            focusedBorderColor = IndustrialGold
                        ),
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = typeDropdownExpanded,
                        onDismissRequest = { typeDropdownExpanded = false }
                    ) {
                        RoomType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { 
                                    Text(type.name.lowercase().replaceFirstChar { it.uppercase() })
                                },
                                onClick = {
                                    roomType = type
                                    typeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Custom Room Name (OPTIONAL)
                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    label = { Text("Custom Room Name (Optional)") },
                    placeholder = { Text("e.g. Master Bedroom, Bedroom 1") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = IndustrialGold,
                        focusedBorderColor = IndustrialGold
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Display Name Preview
                if (customName.isNotBlank()) {
                    Text(
                        text = "Display name: $calculatedDisplayName",
                        style = MaterialTheme.typography.labelSmall,
                        color = IndustrialGold,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Notes (OPTIONAL)
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    placeholder = { Text("Special instructions for this room...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = IndustrialGold,
                        focusedBorderColor = IndustrialGold
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Buttons: Cancel and Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { 
                            onSave(room.copy(
                                roomType = roomType,
                                customName = customName,
                                displayName = calculatedDisplayName,
                                notes = notes.ifBlank { null },
                                updatedAt = System.currentTimeMillis()
                            ))
                        },
                        enabled = canSave,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IndustrialGold,
                            disabledContainerColor = IndustrialGold.copy(alpha = 0.5f)
                        )
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
