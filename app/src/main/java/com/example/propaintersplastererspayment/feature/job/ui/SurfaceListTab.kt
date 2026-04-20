package com.example.propaintersplastererspayment.feature.job.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var showConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF000000)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column {
            // 1. Top accent: Thin horizontal gradient stripe
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color.Black, IndustrialGold, Color.Black)
                        )
                    )
            )

            Column(modifier = Modifier.padding(16.dp)) {
                // 2. Header section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = com.example.propaintersplastererspayment.feature.job.util.SurfaceIconUtils.getIconForSurfaceType(surface.surfaceType),
                        contentDescription = null,
                        tint = IndustrialGold,
                        modifier = Modifier.size(28.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = surface.displayName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = surface.surfaceType.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodySmall,
                            color = IndustrialGold.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Edit button with gold border
                        Surface(
                            onClick = onEdit,
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Transparent,
                            border = BorderStroke(1.dp, IndustrialGold.copy(alpha = 0.5f)),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = IndustrialGold,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Delete button with red border
                        Surface(
                            onClick = { showConfirm = true },
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Transparent,
                            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.Red,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Finish section
                val finish = surface.finishTypeOverride?.takeIf { it.isNotBlank() } ?: "Flat"
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF121212))
                        .drawBehind {
                            val strokeWidth = 4.dp.toPx()
                            drawLine(
                                color = IndustrialGold,
                                start = Offset(strokeWidth / 2, 0f),
                                end = Offset(strokeWidth / 2, size.height),
                                strokeWidth = strokeWidth
                            )
                        }
                        .padding(start = 16.dp, top = 10.dp, bottom = 10.dp, end = 12.dp)
                ) {
                    Column {
                        Text(
                            text = "FINISH TYPE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = finish,
                            style = MaterialTheme.typography.titleMedium,
                            color = IndustrialGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 4. Paint coats section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Undercoat (UC)
                    if (surfaceWithPaint.undercoatPaintId != null) {
                        PaintCoatRow(
                            label = "UC",
                            paintName = "${surfaceWithPaint.undercoatBrandName} ${surfaceWithPaint.undercoatPaintName}",
                            hexCode = surfaceWithPaint.undercoatHexCode,
                            isMainCoat = false
                        )
                    }

                    // Maincoat (MC)
                    if (surfaceWithPaint.maincoatPaintId != null) {
                        PaintCoatRow(
                            label = "MC",
                            paintName = "${surfaceWithPaint.maincoatBrandName} ${surfaceWithPaint.maincoatPaintName} (${surface.maincoatCoatCount} coats)",
                            hexCode = surfaceWithPaint.maincoatHexCode,
                            isMainCoat = true
                        )
                    }
                }
            }
        }
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

@Composable
private fun PaintCoatRow(
    label: String,
    paintName: String,
    hexCode: String?,
    isMainCoat: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0A0A0A),
        border = BorderStroke(
            width = 1.dp,
            color = if (isMainCoat) IndustrialGold.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(parseColor(hexCode ?: "#FFFFFF"))
                    .then(
                        if (hexCode?.lowercase() == "#ffffff") {
                            Modifier.background(Color.White, shape = RoundedCornerShape(16.dp))
                        } else Modifier
                    )
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "$label: $paintName",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isMainCoat) IndustrialGold else Color.Gray,
                fontWeight = if (isMainCoat) FontWeight.SemiBold else FontWeight.Normal
            )
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
