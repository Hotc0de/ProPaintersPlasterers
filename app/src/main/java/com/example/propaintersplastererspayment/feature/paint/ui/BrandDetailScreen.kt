package com.example.propaintersplastererspayment.feature.paint.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
// import androidx.compose.material.icons.filled.Delete (removed unused)
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.example.propaintersplastererspayment.feature.paint.vm.PaintViewModel
import com.example.propaintersplastererspayment.ui.components.ColorSwatch
import com.example.propaintersplastererspayment.ui.components.IndustrialCard
import com.example.propaintersplastererspayment.ui.components.IndustrialFAB
import com.example.propaintersplastererspayment.ui.components.IndustrialTextField
import com.example.propaintersplastererspayment.ui.theme.IndustrialGold
import com.example.propaintersplastererspayment.ui.theme.TextMuted

import com.example.propaintersplastererspayment.ui.components.PrimaryButton

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

    // delete flow removed for now to avoid unused-assignment warnings
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
            if (paints.isNotEmpty()) {
                IndustrialFAB(
                    icon = Icons.Default.Add,
                    onClick = onAddPaint
                )
            }
        },
        containerColor = Color(0xFF0D0D0D)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (paints.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No paints added yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    PrimaryButton(
                        text = "Add Paint",
                        onClick = onAddPaint,
                        modifier = Modifier.width(200.dp),
                        icon = { Icon(Icons.Default.Add, contentDescription = null) }
                    )
                }
            } else if (filteredPaints.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No paints match your search", color = TextMuted)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
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
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    val scope = paint.paintScope.ifBlank { "Interior" }
                                    val scopeColor = if (scope.equals("Exterior", ignoreCase = true)) Color(0xFF4FC3F7) else IndustrialGold
                                    Box(
                                        modifier = Modifier
                                            .background(scopeColor, shape = RoundedCornerShape(10.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = scope,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Black,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    ColorSwatch(hexCode = paint.hexCode, size = 48.dp)
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = paint.paintName,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White
                                    )
                                    if (paint.paintCode.isNotBlank()) {
                                        Text(
                                            text = "Code: ${paint.paintCode}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextMuted
                                        )
                                    }
                                    if (paint.finishType.isNotBlank()) {
                                        Text(
                                            text = paint.finishType,
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

                                // delete action removed
                            }
                        }
                    }
                }
            }
        }
    }

    // delete dialog removed
}
