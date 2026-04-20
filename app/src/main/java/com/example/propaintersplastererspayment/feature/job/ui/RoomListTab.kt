package com.example.propaintersplastererspayment.feature.job.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.propaintersplastererspayment.ProPaintersApplication
import com.example.propaintersplastererspayment.R
import com.example.propaintersplastererspayment.data.local.model.RoomWithSurfaces
import com.example.propaintersplastererspayment.feature.job.util.RoomIconUtils
import com.example.propaintersplastererspayment.feature.job.vm.RoomViewModel
import com.example.propaintersplastererspayment.ui.components.IndustrialFAB
import com.example.propaintersplastererspayment.ui.theme.IndustrialGold
import com.example.propaintersplastererspayment.ui.theme.TextMuted

@Composable
fun RoomListTab(
    jobId: Long,
    modifier: Modifier = Modifier
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
                        onClick = { /* TODO: Navigate to Surface management */ }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoomCard(
    roomWithSurfaces: RoomWithSurfaces,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val room = roomWithSurfaces.room
    val surfaces = roomWithSurfaces.surfaces

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
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

            Column(modifier = Modifier.weight(1f)) {
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

            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = IndustrialGold)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun SurfaceCountBadge(count: Int) {
    Surface(
        color = IndustrialGold.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = if (count == 1) "1 Surface" else "$count Surfaces",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = IndustrialGold
        )
    }
}
