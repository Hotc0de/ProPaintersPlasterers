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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.propaintersplastererspayment.data.local.dao.JobPaintDisplay
import com.example.propaintersplastererspayment.data.local.entity.SurfaceEntity
import com.example.propaintersplastererspayment.data.local.entity.SurfaceType
import com.example.propaintersplastererspayment.ui.theme.CharcoalBackground
import com.example.propaintersplastererspayment.ui.theme.IndustrialGold
import com.example.propaintersplastererspayment.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurfaceFormDialog(
    surface: SurfaceEntity,
    availablePaints: List<JobPaintDisplay>,
    onDismiss: () -> Unit,
    onSave: (SurfaceEntity) -> Unit
) {
    var type by remember { mutableStateOf(surface.surfaceType) }
    var customName by remember { mutableStateOf(surface.customName) }
    var undercoatJobPaintId by remember { mutableStateOf(surface.undercoatJobPaintId) }
    var maincoatJobPaintId by remember { mutableStateOf(surface.maincoatJobPaintId) }
    var maincoatCoatCount by remember { mutableStateOf(surface.maincoatCoatCount.toString()) }
    // Finish options between Undercoat and Maincoat
    val finishOptions = listOf("Flat", "Gloss", "Low Sheen", "Semi-Gloss", "Other")
    var selectedFinish by remember {
        mutableStateOf(
            surface.finishTypeOverride?.let { existing ->
                finishOptions.find { it.equals(existing, ignoreCase = true) } ?: "Other"
            } ?: finishOptions[0]
        )
    }
    var finishSpec by remember { mutableStateOf(if (surface.finishTypeOverride != null && !finishOptions.any { it.equals(surface.finishTypeOverride, ignoreCase = true) }) surface.finishTypeOverride!! else "") }

    var typeExpanded by remember { mutableStateOf(false) }
    var undercoatExpanded by remember { mutableStateOf(false) }
    var maincoatExpanded by remember { mutableStateOf(false) }
    var showTypeError by remember { mutableStateOf(false) }

    val isNew = surface.surfaceId == 0L
    val canSave = type != null

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
                    text = if (isNew) "Add Surface" else "Edit Surface",
                    style = MaterialTheme.typography.headlineSmall,
                    color = IndustrialGold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Surface Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = formatSurfaceTypeName(type),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Surface Type*") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            focusedLabelColor = IndustrialGold,
                            focusedBorderColor = IndustrialGold
                        ),
                        isError = showTypeError
                    )
                    ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        SurfaceType.entries.forEach { surfaceType ->
                            DropdownMenuItem(
                                text = { Text(formatSurfaceTypeName(surfaceType)) },
                                onClick = {
                                    type = surfaceType
                                    typeExpanded = false
                                    showTypeError = false
                                }
                            )
                        }
                    }
                }
                if (showTypeError) {
                    Text(
                        text = "Surface type is required",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Custom Name Field
                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    label = { Text("Custom Name (Optional)") },
                    placeholder = { Text("e.g. Wall 1, Feature Wall, Main Ceiling") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndustrialGold, focusedLabelColor = IndustrialGold)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Undercoat Selection
                ExposedDropdownMenuBox(
                    expanded = undercoatExpanded,
                    onExpandedChange = { undercoatExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = availablePaints.find { it.jobPaintId == undercoatJobPaintId }?.let { "${it.brandName} ${it.paintName}" } ?: "None",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Undercoat") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = undercoatExpanded) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            focusedLabelColor = IndustrialGold,
                            focusedBorderColor = IndustrialGold
                        )
                    )
                    ExposedDropdownMenu(expanded = undercoatExpanded, onDismissRequest = { undercoatExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("None") },
                            onClick = {
                                undercoatJobPaintId = null
                                undercoatExpanded = false
                            }
                        )
                        availablePaints.forEach { paint ->
                            DropdownMenuItem(
                                text = { Text("${paint.brandName} ${paint.paintName}") },
                                onClick = {
                                    undercoatJobPaintId = paint.jobPaintId
                                    undercoatExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Finish Selection (between Undercoat and Maincoat)
                var finishExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = finishExpanded,
                    onExpandedChange = { finishExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedFinish,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Finish") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = finishExpanded) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            focusedLabelColor = IndustrialGold,
                            focusedBorderColor = IndustrialGold
                        )
                    )
                    ExposedDropdownMenu(expanded = finishExpanded, onDismissRequest = { finishExpanded = false }) {
                        finishOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt) },
                                onClick = {
                                    selectedFinish = opt
                                    finishExpanded = false
                                }
                            )
                        }
                    }
                }

                if (selectedFinish == "Other") {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = finishSpec,
                        onValueChange = { finishSpec = it },
                        label = { Text("Specify Finish") },
                        placeholder = { Text("e.g. Low Sheen - Special") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndustrialGold, focusedLabelColor = IndustrialGold)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Maincoat Selection
                ExposedDropdownMenuBox(
                    expanded = maincoatExpanded,
                    onExpandedChange = { maincoatExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = availablePaints.find { it.jobPaintId == maincoatJobPaintId }?.let { "${it.brandName} ${it.paintName}" } ?: "None",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Maincoat") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = maincoatExpanded) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            focusedLabelColor = IndustrialGold,
                            focusedBorderColor = IndustrialGold
                        )
                    )
                    ExposedDropdownMenu(expanded = maincoatExpanded, onDismissRequest = { maincoatExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("None") },
                            onClick = {
                                maincoatJobPaintId = null
                                maincoatExpanded = false
                            }
                        )
                        availablePaints.forEach { paint ->
                            DropdownMenuItem(
                                text = { Text("${paint.brandName} ${paint.paintName}") },
                                onClick = {
                                    maincoatJobPaintId = paint.jobPaintId
                                    maincoatExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Maincoat Apply Count
                OutlinedTextField(
                    value = maincoatCoatCount,
                    onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() }) maincoatCoatCount = it },
                    label = { Text("Number of coats (Maincoat)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndustrialGold, focusedLabelColor = IndustrialGold)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Save/Cancel Buttons
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = TextMuted) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (type == null) {
                                showTypeError = true
                                return@Button
                            }
                            val displayName = if (customName.isNotBlank()) customName else formatSurfaceTypeName(type)
                            onSave(surface.copy(
                                surfaceType = type,
                                customName = customName,
                                displayName = displayName,
                                undercoatJobPaintId = undercoatJobPaintId,
                                maincoatJobPaintId = maincoatJobPaintId,
                                maincoatCoatCount = maincoatCoatCount.toIntOrNull() ?: 2,
                                finishTypeOverride = if (selectedFinish == "Other") finishSpec.trim().takeIf { it.isNotBlank() } else selectedFinish
                            ))
                        },
                        enabled = canSave,
                        colors = ButtonDefaults.buttonColors(containerColor = IndustrialGold)
                    ) {
                        Text("Save Surface")
                    }
                }
            }
        }
    }
}

private fun formatSurfaceTypeName(type: SurfaceType): String {
    return type.name
        .replace("_", " ")
        .lowercase()
        .replaceFirstChar { it.uppercase() }
        .split(" ")
        .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }
}
