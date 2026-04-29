package com.example.propaintersplastererspayment.feature.job.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.propaintersplastererspayment.ProPaintersApplication
import com.example.propaintersplastererspayment.R
import com.example.propaintersplastererspayment.data.local.model.RoomWithSurfaces
import com.example.propaintersplastererspayment.feature.job.util.RoomIconUtils
import com.example.propaintersplastererspayment.feature.job.vm.RoomViewModel
import com.example.propaintersplastererspayment.ui.components.ConfirmDeleteDialog
import com.example.propaintersplastererspayment.ui.components.IndustrialCard
import com.example.propaintersplastererspayment.ui.components.IndustrialFAB
import com.example.propaintersplastererspayment.ui.theme.BorderColor
import com.example.propaintersplastererspayment.ui.theme.CharcoalSecondary
import com.example.propaintersplastererspayment.ui.theme.IndustrialGold
import com.example.propaintersplastererspayment.ui.theme.OffWhite
import com.example.propaintersplastererspayment.ui.theme.TextMuted

@Composable
fun RoomListTab(
    jobId: Long,
    onRoomClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    onShowPaintName: (String) -> Unit = {}
) {
    val application = LocalContext.current.applicationContext as ProPaintersApplication
    val viewModel: RoomViewModel = viewModel(
        factory = RoomViewModel.provideFactory(jobId, application.container.roomRepository)
    )
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = IndustrialGold
            )
        } else if (uiState.rooms.isEmpty()) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No rooms added yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextMuted
                )
                Button(
                    onClick = { viewModel.onAddRoomClick() },
                    colors = ButtonDefaults.buttonColors(containerColor = IndustrialGold),
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Your First Room")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.rooms) { roomWithSurfaces ->
                    RoomCard(
                        roomWithSurfaces = roomWithSurfaces,
                        onEdit = { viewModel.onEditRoomClick(roomWithSurfaces.room) },
                        onDelete = { viewModel.deleteRoom(roomWithSurfaces.room) },
                        onClick = { onRoomClick(roomWithSurfaces.room.roomId) },
                        onShowPaintName = onShowPaintName
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
                }
            }

            // FAB only shows when rooms.isNotEmpty()
            IndustrialFAB(
                onClick = { viewModel.onAddRoomClick() },
                icon = Icons.Default.Add,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
        }

        // Show Room Form Dialog when adding or editing
        uiState.selectedRoom?.let { room ->
            if (uiState.isShowingRoomForm) {
                RoomFormDialog(
                    room = room,
                    onDismiss = { viewModel.onDismissRoomForm() },
                    onSave = { newRoom -> viewModel.saveRoom(newRoom) }
                )
            }
        }
    }
}

@Composable
private fun RoomCard(
    roomWithSurfaces: RoomWithSurfaces,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onShowPaintName: (String) -> Unit = {}
) {
    val room = roomWithSurfaces.room
    val surfaces = roomWithSurfaces.surfaces

    IndustrialCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Room Type Icon
            Icon(
                imageVector = RoomIconUtils.getIconForRoomType(room.roomType),
                contentDescription = room.roomType.name,
                tint = IndustrialGold,
                modifier = Modifier
                    .size(32.dp)
                    .padding(end = 12.dp)
            )

            Column(modifier = Modifier.weight(1.3f)) {
                Text(
                    text = room.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = IndustrialGold
                )
                Text(
                    text = room.roomType.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                SurfaceCountBadge(count = surfaces.size)
            }

            // Action block
            Column(
                modifier = Modifier.weight(0.7f),
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = IndustrialGold)
                    }
                    var showConfirm by remember { mutableStateOf(false) }
                    IconButton(onClick = { showConfirm = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    if (showConfirm) {
                        ConfirmDeleteDialog(
                            title = "Delete Room",
                            message = "Are you sure you want to delete '${room.displayName}' and all its surfaces?",
                            onConfirm = {
                                showConfirm = false
                                onDelete()
                            },
                            onDismiss = { showConfirm = false }
                        )
                    }
                }

                // Main Coat Color + Paint Name
                roomWithSurfaces.maincoatHexCode?.let { hex ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(parseColor(hex))
                                .then(if(hex.lowercase() == "#ffffff") Modifier.border(0.5.dp, BorderColor, RoundedCornerShape(4.dp)) else Modifier)
                                .clickable {
                                    roomWithSurfaces.maincoatPaintName?.let { name ->
                                        onShowPaintName(name)
                                    }
                                }
                        )
                        roomWithSurfaces.maincoatPaintName?.let { name ->
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = name,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SurfaceCountBadge(count: Int) {
    Surface(
        color = CharcoalSecondary,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Text(
            text = if (count == 1) "1 Surface" else "$count Surfaces",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = OffWhite
        )
    }
}

private fun parseColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Color.Transparent
    }
}
