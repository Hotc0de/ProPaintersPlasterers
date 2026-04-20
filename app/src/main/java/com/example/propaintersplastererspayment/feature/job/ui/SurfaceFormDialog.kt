package com.example.propaintersplastererspayment.feature.job.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
    var label by remember { mutableStateOf(surface.surfaceLabel) }
    var type by remember { mutableStateOf(surface.surfaceType) }
    var selectedPaintId by remember { mutableStateOf(surface.selectedJobPaintId) }
    var finishType by remember { mutableStateOf(surface.finishTypeOverride ?: "") }
    var coatCount by remember { mutableIntStateOf(surface.coatCount) }
    var isFeature by remember { mutableStateOf(surface.isFeatureSurface) }
    var notes by remember { mutableStateOf(surface.notes ?: "") }

    var typeExpanded by remember { mutableStateOf(false) }
    var paintExpanded by remember { mutableStateOf(false) }

    val isNew = surface.surfaceId == 0L
    val canSave = label.isNotBlank()

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

                // Surface Type
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = type.name.lowercase().replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Surface Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            focusedLabelColor = IndustrialGold,
                            focusedBorderColor = IndustrialGold
                        )
                    )
                    ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        SurfaceType.entries.forEach { surfaceType ->
                            DropdownMenuItem(
                                text = { Text(surfaceType.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    type = surfaceType
                                    if (label.isBlank()) {
                                        label = surfaceType.name.lowercase().replaceFirstChar { it.uppercase() }
                                    }
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Surface Label
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Surface Label") },
                    placeholder = { Text("e.g. South Wall, Main Ceiling") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndustrialGold, focusedLabelColor = IndustrialGold)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Paint Selector (Job Palette Only)
                ExposedDropdownMenuBox(
                    expanded = paintExpanded,
                    onExpandedChange = { paintExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val selectedPaint = availablePaints.find { it.jobPaintId == selectedPaintId }
                    OutlinedTextField(
                        value = selectedPaint?.let { "${it.brandName} ${it.paintName}" } ?: "Select Paint",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Assigned Paint") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paintExpanded) },
                        leadingIcon = selectedPaint?.let {
                            {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(parseColor(it.hexCode))
                                )
                            }
                        },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            focusedLabelColor = IndustrialGold,
                            focusedBorderColor = IndustrialGold
                        )
                    )
                    ExposedDropdownMenu(expanded = paintExpanded, onDismissRequest = { paintExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("None / No Paint") },
                            onClick = {
                                selectedPaintId = null
                                paintExpanded = false
                            }
                        )
                        availablePaints.forEach { paint ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(parseColor(paint.hexCode))
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("${paint.brandName} ${paint.paintName}")
                                    }
                                },
                                onClick = {
                                    selectedPaintId = paint.jobPaintId
                                    paintExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    // Finish Type
                    OutlinedTextField(
                        value = finishType,
                        onValueChange = { finishType = it },
                        label = { Text("Finish") },
                        placeholder = { Text("e.g. Low Sheen") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndustrialGold, focusedLabelColor = IndustrialGold)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Coat Count
                    OutlinedTextField(
                        value = coatCount.toString(),
                        onValueChange = { coatCount = it.toIntOrNull() ?: 2 },
                        label = { Text("Coats") },
                        modifier = Modifier.width(80.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndustrialGold, focusedLabelColor = IndustrialGold)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Feature Surface Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Is Feature Surface?", color = TextMuted)
                    Switch(
                        checked = isFeature,
                        onCheckedChange = { isFeature = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = IndustrialGold, checkedTrackColor = IndustrialGold.copy(alpha = 0.5f))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndustrialGold, focusedLabelColor = IndustrialGold)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Buttons
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = TextMuted) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(surface.copy(
                                surfaceLabel = label,
                                surfaceType = type,
                                selectedJobPaintId = selectedPaintId,
                                finishTypeOverride = finishType.ifBlank { null },
                                coatCount = coatCount,
                                isFeatureSurface = isFeature,
                                notes = notes.ifBlank { null }
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

private fun parseColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Color.Gray
    }
}
