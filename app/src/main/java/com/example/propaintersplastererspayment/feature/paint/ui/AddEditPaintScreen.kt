package com.example.propaintersplastererspayment.feature.paint.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.propaintersplastererspayment.core.util.PaintColorUtils
import com.example.propaintersplastererspayment.feature.paint.vm.PaintViewModel
import com.example.propaintersplastererspayment.ui.components.ColorSwatch
import com.example.propaintersplastererspayment.ui.components.IndustrialFAB
import com.example.propaintersplastererspayment.ui.components.IndustrialTextField
import com.example.propaintersplastererspayment.ui.theme.IndustrialGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPaintScreen(
    brandId: Long,
    paintId: Long?,
    onBack: () -> Unit,
    viewModel: PaintViewModel
) {
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var hex by remember { mutableStateOf("") }
    var finishType by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(paintId != null) }

    // If editing, load existing data
    if (paintId != null) {
        val paintFlow = remember(paintId) { viewModel.getPaintById(paintId) }
        val paintDetail by paintFlow.collectAsState()
        
        LaunchedEffect(paintDetail) {
            if (paintDetail != null) {
                name = paintDetail?.paintName ?: ""
                code = paintDetail?.paintCode ?: ""
                hex = paintDetail?.hexCode ?: ""
                finishType = paintDetail?.finishType ?: ""
                notes = paintDetail?.notes ?: ""
                isLoading = false
            } else if (!isLoading && paintId != null) {
                // If it's null and we were loading, it means it's not found
                // (Optional: handle error or just stop loading)
                // isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (paintId == null) "Add Paint" else "Edit Paint", fontWeight = FontWeight.Bold) },
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
            if (!isLoading) {
                IndustrialFAB(
                    icon = Icons.Default.Done,
                    onClick = {
                        if (name.isNotBlank()) {
                            if (paintId == null) {
                                viewModel.addPaint(
                                    brandId = brandId,
                                    name = name,
                                    code = code,
                                    hex = PaintColorUtils.normalizeHexCode(hex),
                                    finishType = finishType,
                                    notes = notes
                                )
                            } else {
                                viewModel.updatePaint(
                                    paintId = paintId,
                                    brandId = brandId,
                                    name = name,
                                    code = code,
                                    hex = PaintColorUtils.normalizeHexCode(hex),
                                    finishType = finishType,
                                    notes = notes
                                )
                            }
                            onBack()
                        }
                    }
                )
            }
        },
        containerColor = Color(0xFF0D0D0D)
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = IndustrialGold)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Live Preview Section
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 10.dp)
                ) {
                    ColorSwatch(hexCode = hex, size = 100.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (PaintColorUtils.isValidHexCode(hex)) "COLOR PREVIEW" else "INVALID HEX CODE",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (PaintColorUtils.isValidHexCode(hex)) IndustrialGold else Color.Red.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold
                    )
                }

                IndustrialTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Paint Name (e.g. Ironstone)",
                    modifier = Modifier.fillMaxWidth()
                )

                IndustrialTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = "Paint Code (Optional)",
                    modifier = Modifier.fillMaxWidth()
                )

                IndustrialTextField(
                    value = finishType,
                    onValueChange = { finishType = it },
                    label = "Finish (e.g. Low Sheen, Gloss, Matt)",
                    modifier = Modifier.fillMaxWidth()
                )

                IndustrialTextField(
                    value = hex,
                    onValueChange = { hex = it },
                    label = "Hex Code (e.g. #3C3F41)",
                    modifier = Modifier.fillMaxWidth()
                )

                IndustrialTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "Notes",
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    minLines = 3
                )
            }
        }
    }
}
