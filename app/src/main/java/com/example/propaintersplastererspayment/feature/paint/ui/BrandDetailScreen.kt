package com.example.propaintersplastererspayment.feature.paint.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.propaintersplastererspayment.feature.paint.vm.PaintViewModel
import com.example.propaintersplastererspayment.ui.components.ColorSwatch
import com.example.propaintersplastererspayment.ui.components.IndustrialCard
import com.example.propaintersplastererspayment.ui.components.IndustrialFAB
import com.example.propaintersplastererspayment.ui.components.IndustrialTextField
import com.example.propaintersplastererspayment.ui.theme.IndustrialGold
import com.example.propaintersplastererspayment.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrandDetailScreen(
    brandId: Long,
    onAddPaint: () -> Unit,
    onEditPaint: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: PaintViewModel
) {
    val brands by viewModel.brands.collectAsState()
    val brand = brands.find { it.brandId == brandId }
    
    // Stabilize the flow to prevent "Librarian Loop" (infinite flashing)
    val paintsFlow = remember(brandId) { viewModel.getPaintsForBrand(brandId) }
    val paints by paintsFlow.collectAsState()

    var paintToDelete by remember { mutableStateOf<com.example.propaintersplastererspayment.data.local.entity.PaintItemEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredPaints = remember(paints, searchQuery) {
        if (searchQuery.isBlank()) {
            paints
        } else {
            paints.filter {
                it.paintName.contains(searchQuery, ignoreCase = true) ||
                it.paintCode.contains(searchQuery, ignoreCase = true) ||
                it.finishType.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(brand?.brandName ?: "Paints", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF0D0D0D),
                        titleContentColor = IndustrialGold,
                        navigationIconContentColor = IndustrialGold
                    )
                )
                
                // Search Bar
                IndustrialTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = "Search paints...",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        },
        floatingActionButton = {
            IndustrialFAB(
                icon = Icons.Default.Add,
                onClick = onAddPaint
            )
        },
        containerColor = Color(0xFF0D0D0D)
    ) { padding ->
        if (paints.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No paints added yet", color = TextMuted)
            }
        } else if (filteredPaints.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No paints match your search", color = TextMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredPaints) { paint ->
                    IndustrialCard(
                        modifier = Modifier.clickable { onEditPaint(paint.paintId) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ColorSwatch(hexCode = paint.hexCode, size = 48.dp)
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = paint.paintName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White
                                )
                                if (paint.paintCode.isNotBlank() || paint.finishType.isNotBlank()) {
                                    val subtitle = listOfNotNull(
                                        if (paint.paintCode.isNotBlank()) "Code: ${paint.paintCode}" else null,
                                        if (paint.finishType.isNotBlank()) paint.finishType else null
                                    ).joinToString(" • ")
                                    Text(
                                        text = subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted
                                    )
                                }
                                Text(
                                    text = paint.hexCode.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = IndustrialGold.copy(alpha = 0.7f)
                                )
                            }

                            IconButton(onClick = { paintToDelete = paint }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Paint",
                                    tint = Color.Red.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (paintToDelete != null) {
        AlertDialog(
            onDismissRequest = { paintToDelete = null },
            title = { Text("Delete Paint", color = Color.White) },
            text = { Text("Are you sure you want to delete ${paintToDelete?.paintName}?", color = Color.White) },
            confirmButton = {
                TextButton(onClick = {
                    paintToDelete?.let { viewModel.deletePaint(it) }
                    paintToDelete = null
                }) {
                    Text("DELETE", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { paintToDelete = null }) {
                    Text("CANCEL", color = IndustrialGold)
                }
            },
            containerColor = Color(0xFF1A1A1A)
        )
    }
}
