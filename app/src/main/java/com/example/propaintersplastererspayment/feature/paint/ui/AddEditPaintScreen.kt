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
    // Hex field always starts with '#' and cannot be removed by the user.
    var hex by remember { mutableStateOf("#") }
    // finish options: if user selects "Specify", they can enter a custom finish
    val finishOptions = listOf("Flat", "Gloss", "Low Sheen", "Semi-Gloss", "Specify")
    var selectedFinish by remember { mutableStateOf(finishOptions[0]) }
    var finishSpec by remember { mutableStateOf("") }
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
                // Ensure hex is normalized to start with '#' and uppercase for preview and editing
                hex = paintDetail?.hexCode?.let { code ->
                    if (code.isBlank()) "#" else {
                        val cleaned = if (code.startsWith("#")) code.substring(1) else code
                        "#${cleaned.uppercase()}"
                    }
                } ?: "#"
                // Pre-fill finish selection: if matches known option, select it; otherwise use Specify + fill spec
                val existingFinish = paintDetail?.finishType ?: ""
                if (existingFinish.isNotBlank()) {
                    val matched = finishOptions.find { it.equals(existingFinish, ignoreCase = true) }
                    if (matched != null && matched != "Specify") {
                        selectedFinish = matched
                        finishSpec = ""
                    } else {
                        selectedFinish = "Specify"
                        finishSpec = existingFinish
                    }
                } else {
                    selectedFinish = finishOptions[0]
                    finishSpec = ""
                }
                notes = paintDetail?.notes ?: ""
                isLoading = false
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
                                    val finalFinish = if (selectedFinish == "Specify") finishSpec.trim() else selectedFinish
                                    if (paintId == null) {
                                        viewModel.addPaint(
                                            brandId = brandId,
                                            name = name,
                                            code = code,
                                            hex = PaintColorUtils.normalizeHexCode(hex),
                                            finishType = finalFinish,
                                            notes = notes
                                        )
                                    } else {
                                        viewModel.updatePaint(
                                            paintId = paintId,
                                            brandId = brandId,
                                            name = name,
                                            code = code,
                                            hex = PaintColorUtils.normalizeHexCode(hex),
                                            finishType = finalFinish,
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

                // Finish dropdown with preset options + 'Specify' for custom input
                var finishExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = finishExpanded,
                    onExpandedChange = { finishExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedFinish,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Finish") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = finishExpanded) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(expanded = finishExpanded, onDismissRequest = { finishExpanded = false }) {
                        finishOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt) },
                                onClick = {
                                    selectedFinish = opt
                                    finishExpanded = false
                                }
                            )
                        }
                    }
                }

                if (selectedFinish == "Specify") {
                    IndustrialTextField(
                        value = finishSpec,
                        onValueChange = { finishSpec = it },
                        label = "Specify Finish",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                IndustrialTextField(
                    value = hex,
                    onValueChange = { input ->
                        // Normalize input so it always starts with '#', uppercase, and only contains hex chars
                        var v = input.uppercase()
                        if (v.isEmpty()) {
                            v = "#"
                        }
                        if (!v.startsWith("#")) v = "#${v}"

                        // keep only hex characters after the leading '#'
                        val cleaned = v.drop(1).filter { ch ->
                            ch.isDigit() || (ch in 'A'..'F')
                        }

                        // limit to 6 hex digits
                        val limited = if (cleaned.length > 6) cleaned.substring(0, 6) else cleaned

                        hex = "#${limited}"
                    },
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
