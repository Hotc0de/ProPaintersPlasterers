package com.example.propaintersplastererspayment.feature.paint.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.propaintersplastererspayment.ProPaintersApplication
import com.example.propaintersplastererspayment.feature.paint.vm.PaintViewModel
import com.example.propaintersplastererspayment.ui.components.IndustrialCard
import com.example.propaintersplastererspayment.ui.components.IndustrialFAB
import com.example.propaintersplastererspayment.ui.components.IndustrialTextField
import com.example.propaintersplastererspayment.ui.theme.IndustrialGold

import com.example.propaintersplastererspayment.ui.components.PrimaryButton
import com.example.propaintersplastererspayment.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaintBrandListScreen(
    onBrandClick: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: PaintViewModel
) {
    val brands by viewModel.brands.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var newBrandName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paint Brands", fontWeight = FontWeight.Bold) },
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
        },
        floatingActionButton = {
            if (brands.isNotEmpty()) {
                IndustrialFAB(
                    icon = Icons.Default.Add,
                    onClick = { showAddDialog = true }
                )
            }
        },
        containerColor = Color(0xFF0D0D0D)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (brands.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No paint brands yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    PrimaryButton(
                        text = "Add Paint Brand",
                        onClick = { showAddDialog = true },
                        modifier = Modifier.width(200.dp),
                        icon = { Icon(Icons.Default.Add, contentDescription = null) }
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(brands) { brand ->
                        IndustrialCard(
                            modifier = Modifier.clickable { onBrandClick(brand.brandId) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = brand.brandName,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White
                                )
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = IndustrialGold.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Add Paint Brand", color = Color.White) },
                text = {
                    IndustrialTextField(
                        value = newBrandName,
                        onValueChange = { newBrandName = it },
                        label = "Brand Name",
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newBrandName.isNotBlank()) {
                                viewModel.addBrand(newBrandName)
                                newBrandName = ""
                                showAddDialog = false
                            }
                        }
                    ) {
                        Text("ADD", color = IndustrialGold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("CANCEL", color = Color.Gray)
                    }
                },
                containerColor = Color(0xFF1A1A1A)
            )
        }
    }
}
