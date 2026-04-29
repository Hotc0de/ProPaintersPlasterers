package com.example.propaintersplastererspayment.feature.job.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.propaintersplastererspayment.ProPaintersApplication
import com.example.propaintersplastererspayment.core.pdf.PdfExportService
import com.example.propaintersplastererspayment.core.pdf.PdfFileHelper
import com.example.propaintersplastererspayment.feature.job.vm.CalculatorViewModel
import com.example.propaintersplastererspayment.ui.theme.*
import java.util.*

data class CalculationItemUi(
    val id: String = UUID.randomUUID().toString(),
    val areaName: String = "",
    val quantity: String = "",
    val costPerUnit: String = "",
    val isSqM: Boolean = true,
    val includeGst: Boolean = true
)

@Composable
fun CalculatorRoute(jobId: Long) {
    val application = androidx.compose.ui.platform.LocalContext.current.applicationContext as ProPaintersApplication
    val viewModel: CalculatorViewModel = viewModel(
        factory = CalculatorViewModel.provideFactory(
            jobId = jobId,
            jobRepository = application.container.jobRepository,
            settingsRepository = application.container.settingsRepository
        )
    )

    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.pdfExportEvents.collect { data ->
            try {
                val file = PdfFileHelper.createExportFile(context, data.fileName)
                PdfExportService().exportCalculationPdf(data, file)
                PdfFileHelper.sharePdf(context, file, "Share Calculation PDF")
                viewModel.onPdfExportFinished(true)
            } catch (e: Exception) {
                e.printStackTrace()
                viewModel.onPdfExportFinished(false)
            }
        }
    }

    uiState.userMessage?.let { message ->
        LaunchedEffect(message) {
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearUserMessage()
        }
    }

    CalculatorScreen(
        items = uiState.items,
        onAddItem = viewModel::addItem,
        onUpdateItem = viewModel::updateItem,
        onDeleteItem = viewModel::deleteItem,
        onExportPdf = viewModel::exportPdf,
        onExportItemPdf = viewModel::exportSingleItemPdf
    )
}

@Composable
fun CalculatorScreen(
    items: List<CalculationItemUi>,
    onAddItem: () -> Unit,
    onUpdateItem: (CalculationItemUi) -> Unit,
    onDeleteItem: (String) -> Unit,
    onExportPdf: () -> Unit,
    onExportItemPdf: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CharcoalBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Price Calculator",
                style = MaterialTheme.typography.titleLarge,
                color = IndustrialGold,
                fontWeight = FontWeight.Bold
            )
            
            Button(
                onClick = onAddItem,
                colors = ButtonDefaults.buttonColors(
                    containerColor = IndustrialGold,
                    contentColor = CharcoalBackground
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Add Area", fontWeight = FontWeight.Bold)
            }

            IconButton(onClick = onExportPdf) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = "Export PDF",
                    tint = IndustrialGold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(items, key = { it.id }) { item ->
                CalculationCard(
                    item = item,
                    onUpdate = onUpdateItem,
                    onDelete = { onDeleteItem(item.id) },
                    onExportPdf = { onExportItemPdf(item.id) }
                )
            }
        }

        HorizontalDivider(color = IndustrialGold.copy(alpha = 0.3f), thickness = 1.dp)

        // Calculate grand total
        val subtotal = items.sumOf { (it.quantity.toDoubleOrNull() ?: 0.0) * (it.costPerUnit.toDoubleOrNull() ?: 0.0) }
        val gstTotal = items.filter { it.includeGst }.sumOf { (it.quantity.toDoubleOrNull() ?: 0.0) * (it.costPerUnit.toDoubleOrNull() ?: 0.0) * 0.15 }
        val grandTotal = subtotal + gstTotal

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "Total Amount: $${String.format(Locale.getDefault(), "%.2f", grandTotal)}",
                color = IndustrialGold,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SummaryRow(
    label: String,
    amount: Double,
    color: Color,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isBold) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium,
            color = if (isBold) TextMuted else color,
            fontWeight = if (isBold) FontWeight.Medium else FontWeight.Normal
        )
        Text(
            text = "$${String.format(Locale.getDefault(), "%.2f", amount)}",
            style = if (isBold) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun CalculationCard(
    item: CalculationItemUi,
    onUpdate: (CalculationItemUi) -> Unit,
    onDelete: () -> Unit,
    onExportPdf: () -> Unit
) {
    Surface(
        color = CharcoalCard,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = item.areaName,
                    onValueChange = { onUpdate(item.copy(areaName = it)) },
                    label = { Text("Area Name (e.g. Lounge)", color = TextMuted) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = IndustrialGold,
                        unfocusedBorderColor = TextMuted.copy(alpha = 0.5f),
                        cursorColor = IndustrialGold
                    ),
                    singleLine = true
                )
                
                IconButton(onClick = onExportPdf) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF", tint = IndustrialGold)
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (item.isSqM) "m²" else "m",
                        color = IndustrialGold.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelMedium
                    )
                    TextButton(
                        onClick = { onUpdate(item.copy(isSqM = !item.isSqM)) }
                    ) {
                        Text(
                            text = "Switch to ${if (item.isSqM) "m" else "m²"}",
                            color = IndustrialGold,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "GST",
                        color = TextMuted,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Switch(
                        checked = item.includeGst,
                        onCheckedChange = { onUpdate(item.copy(includeGst = it)) },
                        modifier = Modifier.scale(0.7f),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = IndustrialGold,
                            checkedTrackColor = IndustrialGold.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = item.quantity,
                    onValueChange = { onUpdate(item.copy(quantity = it)) },
                    label = { Text(if (item.isSqM) "m2" else "m", color = TextMuted) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = IndustrialGold,
                        unfocusedBorderColor = TextMuted.copy(alpha = 0.5f),
                        cursorColor = IndustrialGold
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = item.costPerUnit,
                    onValueChange = { onUpdate(item.copy(costPerUnit = it)) },
                    label = { Text(if (item.isSqM) "$/m2" else "$/m", color = TextMuted) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = IndustrialGold,
                        unfocusedBorderColor = TextMuted.copy(alpha = 0.5f),
                        cursorColor = IndustrialGold
                    ),
                    singleLine = true
                )
            }

            val qty = item.quantity.toDoubleOrNull() ?: 0.0
            val cost = item.costPerUnit.toDoubleOrNull() ?: 0.0
            val rowTotal = qty * cost

            if (rowTotal > 0) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    if (item.includeGst) {
                        val gst = rowTotal * 0.15
                        val total = rowTotal + gst
                        Text(
                            text = "Subtotal: $${String.format(Locale.getDefault(), "%.2f", rowTotal)}",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "GST (15%): $${String.format(Locale.getDefault(), "%.2f", gst)}",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Total (Incl. GST): $${String.format(Locale.getDefault(), "%.2f", total)}",
                            color = IndustrialGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    } else {
                        Text(
                            text = "Total: $${String.format(Locale.getDefault(), "%.2f", rowTotal)}",
                            color = IndustrialGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CalculatorPreview() {
    ProPaintersTheme {
        CalculatorScreen(
            items = listOf(
                CalculationItemUi(areaName = "Lounge", quantity = "30", costPerUnit = "12"),
                CalculationItemUi(areaName = "Skirting", quantity = "15", costPerUnit = "12", isSqM = false, includeGst = false)
            ),
            onAddItem = {},
            onUpdateItem = {},
            onDeleteItem = {},
            onExportPdf = {},
            onExportItemPdf = {}
        )
    }
}
