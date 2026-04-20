package com.example.propaintersplastererspayment.feature.job.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.propaintersplastererspayment.ProPaintersApplication
import com.example.propaintersplastererspayment.data.local.model.SurfaceWithJobPaint
import com.example.propaintersplastererspayment.feature.job.vm.SurfaceViewModel
import com.example.propaintersplastererspayment.ui.components.IndustrialFAB
import com.example.propaintersplastererspayment.ui.theme.IndustrialGold
import com.example.propaintersplastererspayment.ui.theme.TextMuted

@Composable
fun SurfaceListTab(
    jobId: Long,
    roomId: Long,
    modifier: Modifier = Modifier
) {
    val application = LocalContext.current.applicationContext as ProPaintersApplication
    val viewModel: SurfaceViewModel = viewModel(
        factory = SurfaceViewModel.provideFactory(
            jobId,
            roomId,
            application.container.roomRepository,
            application.container.paintRepository
        )
    )
    val uiState by viewModel.uiState.collectAsState()
    val room by application.container.roomRepository.observeRoom(roomId).collectAsState(initial = null)

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Room Title
            room?.let {
                Text(
                    text = "Surfaces for ${it.room.displayName}",
                    style = MaterialTheme.typography.titleMedium,
                    color = IndustrialGold,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = IndustrialGold)
                }
            } else if (uiState.surfaces.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No surfaces in this room",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextMuted
                    )
                    Button(
                        onClick = { viewModel.onAddSurfaceClick() },
                        colors = ButtonDefaults.buttonColors(containerColor = IndustrialGold),
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add Surface")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.surfaces) { surfaceWithPaint ->
                        SurfaceRow(
                            surfaceWithPaint = surfaceWithPaint,
                            onEdit = { viewModel.onEditSurfaceClick(surfaceWithPaint.surface) },
                            onDelete = { viewModel.deleteSurface(surfaceWithPaint.surface) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp)) // FAB space
                    }
                }
            }
        }

        if (uiState.surfaces.isNotEmpty()) {
            IndustrialFAB(
                onClick = { viewModel.onAddSurfaceClick() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
        }
    }

    if (uiState.isShowingSurfaceForm && uiState.selectedSurface != null) {
        SurfaceFormDialog(
            surface = uiState.selectedSurface!!,
            availablePaints = uiState.availablePaints,
            onDismiss = { viewModel.onDismissSurfaceForm() },
            onSave = { viewModel.saveSurface(it) }
        )
    }
}

@Composable
private fun SurfaceRow(
    surfaceWithPaint: SurfaceWithJobPaint,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val surface = surfaceWithPaint.surface

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Surface icon
            Icon(
                imageVector = com.example.propaintersplastererspayment.feature.job.util.SurfaceIconUtils.getIconForSurfaceType(surface.surfaceType),
                contentDescription = surface.surfaceType.name,
                tint = IndustrialGold,
                modifier = Modifier
                    .size(32.dp)
                    .padding(end = 12.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = surface.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = surface.surfaceType.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodySmall,
                    color = IndustrialGold
                )
                // Show finish type if set
                surface.finishTypeOverride?.takeIf { it.isNotBlank() }?.let { finish ->
                    Spacer(modifier = Modifier.height(6.dp))
                    // Highlight non-Flat finishes (e.g., Low Sheen, Semi-Gloss, Gloss, Other)
                    val isHighlighted = !finish.equals("Flat", ignoreCase = true)
                    val finishColor = if (isHighlighted) IndustrialGold else TextMuted
                    val finishWeight = if (isHighlighted) FontWeight.SemiBold else FontWeight.Normal
                    val pillBg = if (isHighlighted) IndustrialGold.copy(alpha = 0.10f) else Color.Transparent
                    Text(
                        text = "Finish: ${finish}",
                        style = MaterialTheme.typography.bodySmall,
                        color = finishColor,
                        fontWeight = finishWeight,
                        modifier = Modifier
                            .background(pillBg, shape = RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                
                // Undercoat
                if (surfaceWithPaint.undercoatPaintId != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (surfaceWithPaint.undercoatHexCode != null) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(parseColor(surfaceWithPaint.undercoatHexCode))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = "UC: ${surfaceWithPaint.undercoatBrandName} ${surfaceWithPaint.undercoatPaintName}",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextMuted
                        )
                    }
                }

                // Maincoat
                if (surfaceWithPaint.maincoatPaintId != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (surfaceWithPaint.maincoatHexCode != null) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(parseColor(surfaceWithPaint.maincoatHexCode))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = "MC: ${surfaceWithPaint.maincoatBrandName} ${surfaceWithPaint.maincoatPaintName} (${surface.maincoatCoatCount} coats)",
                            style = MaterialTheme.typography.labelMedium,
                            color = IndustrialGold
                        )
                    }
                }
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = IndustrialGold, modifier = Modifier.size(20.dp))
                }
                var showConfirm by remember { mutableStateOf(false) }
                IconButton(onClick = { showConfirm = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                }
                if (showConfirm) {
                    com.example.propaintersplastererspayment.ui.components.ConfirmDeleteDialog(
                        title = "Delete Surface",
                        message = "Are you sure you want to delete '${surface.displayName}'?",
                        onConfirm = {
                            showConfirm = false
                            onDelete()
                        },
                        onDismiss = { showConfirm = false }
                    )
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
