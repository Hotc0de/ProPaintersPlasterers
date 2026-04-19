package com.example.propaintersplastererspayment.feature.job.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.propaintersplastererspayment.ProPaintersApplication
import com.example.propaintersplastererspayment.data.local.entity.JobPaintEntity
import com.example.propaintersplastererspayment.feature.paint.vm.PaintViewModel
import com.example.propaintersplastererspayment.ui.components.ColorSwatch
import com.example.propaintersplastererspayment.ui.components.IndustrialCard
import com.example.propaintersplastererspayment.ui.components.IndustrialFAB
import com.example.propaintersplastererspayment.ui.theme.IndustrialGold
import com.example.propaintersplastererspayment.ui.theme.TextMuted
import kotlinx.coroutines.launch

@Composable
fun JobPaintRoute(jobId: Long) {
    val application = LocalContext.current.applicationContext as ProPaintersApplication
    val viewModel: PaintViewModel = viewModel(
        factory = PaintViewModel.provideFactory(application.container.paintRepository)
    )
    
    val jobPaints by viewModel.paintRepository.getPaintsForJobStream(jobId).collectAsState(initial = emptyList())
    val allPaints by viewModel.paintRepository.getAllPaintsWithBrandStream().collectAsState(initial = emptyList())
    val brands by viewModel.brands.collectAsState()
    val scope = rememberCoroutineScope()

    var showSelectDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (jobPaints.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No paints selected for this job", color = TextMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(jobPaints) { jp ->
                    IndustrialCard {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ColorSwatch(hexCode = jp.hexCode, size = 40.dp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(jp.brandName, style = MaterialTheme.typography.labelSmall, color = IndustrialGold)
                                Text(jp.paintName, style = MaterialTheme.typography.titleMedium, color = Color.White)
                                if (jp.paintCode.isNotBlank()) {
                                    Text("Code: ${jp.paintCode}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                }
                            }
                            IconButton(onClick = { 
                                scope.launch {
                                    viewModel.paintRepository.removePaintFromJob(jp.jobPaintId)
                                }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }
        }

        IndustrialFAB(
            icon = Icons.Default.Add,
            onClick = { showSelectDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        )
    }

    if (showSelectDialog) {
        val availablePaints = allPaints.filter { p -> jobPaints.none { it.paintId == p.paintId } }
        
        AlertDialog(
            onDismissRequest = { showSelectDialog = false },
            title = { Text("Select Paint", color = Color.White) },
            text = {
                if (availablePaints.isEmpty()) {
                    Text("No more paints available in library.", color = TextMuted)
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availablePaints) { paint ->
                            val brandName = brands.find { it.brandId == paint.brandId }?.brandName ?: ""
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        scope.launch {
                                            viewModel.paintRepository.addPaintToJob(
                                                JobPaintEntity(jobId = jobId, paintId = paint.paintId)
                                            )
                                            showSelectDialog = false
                                        }
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ColorSwatch(hexCode = paint.hexCode, size = 32.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(brandName, style = MaterialTheme.typography.labelSmall, color = IndustrialGold)
                                    Text(paint.paintName, color = Color.White)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSelectDialog = false }) {
                    Text("CANCEL", color = IndustrialGold)
                }
            },
            containerColor = Color(0xFF1A1A1A)
        )
    }
}
