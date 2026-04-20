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
    var name by remember { mutableStateOf(room.roomName) }
    var type by remember { mutableStateOf(room.roomType) }
    var level by remember { mutableStateOf(room.level ?: "") }
    var notes by remember { mutableStateOf(room.notes ?: "") }
    
    var typeDropdownExpanded by remember { mutableStateOf(false) }
    
    val isNew = room.roomId == 0L
    val canSave = name.isNotBlank()

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

                // Room Type
                ExposedDropdownMenuBox(
                    expanded = typeDropdownExpanded,
                    onExpandedChange = { typeDropdownExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = type.name.lowercase().replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Room Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            focusedLabelColor = IndustrialGold,
                            focusedBorderColor = IndustrialGold
                        ),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = typeDropdownExpanded,
                        onDismissRequest = { typeDropdownExpanded = false }
                    ) {
                        RoomType.entries.forEach { roomType ->
                            DropdownMenuItem(
                                text = { 
                                    Text(roomType.name.lowercase().replaceFirstChar { it.uppercase() }) 
                                },
                                onClick = {
                                    type = roomType
                                    if (name.isBlank()) {
                                        name = roomType.name.lowercase().replaceFirstChar { it.uppercase() }
                                    }
                                    typeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Room Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Room Name") },
                    placeholder = { Text("e.g. Master Bedroom") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = IndustrialGold,
                        focusedBorderColor = IndustrialGold
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Level / Floor
                OutlinedTextField(
                    value = level,
                    onValueChange = { level = it },
                    label = { Text("Level / Floor (Optional)") },
                    placeholder = { Text("e.g. Ground Floor, Level 1") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = IndustrialGold,
                        focusedBorderColor = IndustrialGold
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    placeholder = { Text("Specific instructions for this room...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = IndustrialGold,
                        focusedBorderColor = IndustrialGold
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Buttons
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
                                roomName = name,
                                roomType = type,
                                level = level.ifBlank { null },
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
                        Text("Save Room")
                    }
                }
            }
        }
    }
}
