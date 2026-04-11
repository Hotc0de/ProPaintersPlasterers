package com.example.propaintersplastererspayment.feature.client.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.propaintersplastererspayment.ProPaintersApplication
import com.example.propaintersplastererspayment.core.ui.isCompactPhoneWidth
import com.example.propaintersplastererspayment.feature.client.vm.AddEditClientUiState
import com.example.propaintersplastererspayment.feature.client.vm.AddEditClientViewModel
import com.example.propaintersplastererspayment.feature.client.vm.ClientFormState
import com.example.propaintersplastererspayment.ui.theme.GoldAccent
import com.example.propaintersplastererspayment.ui.theme.OxfordBlue

@Composable
fun AddEditClientRoute(
    clientId: Long?,
    onDone: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    val application = LocalContext.current.applicationContext as ProPaintersApplication
    val viewModel: AddEditClientViewModel = viewModel(
        factory = AddEditClientViewModel.provideFactory(
            clientId = clientId,
            clientRepository = application.container.clientRepository
        )
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.savedEvent.collect { savedClientId ->
            onDone(savedClientId.takeIf { it > 0L })
        }
    }

    AddEditClientScreen(
        uiState = uiState,
        onNameChange = viewModel::onNameChange,
        onClientTypeChange = viewModel::onClientTypeChange,
        onAddressChange = viewModel::onAddressChange,
        onPhoneChange = viewModel::onPhoneChange,
        onEmailChange = viewModel::onEmailChange,
        onNotesChange = viewModel::onNotesChange,
        onSave = viewModel::saveClient,
        onDelete = viewModel::deleteClient,
        onBack = { onDone(null) },
        onMessageShown = viewModel::clearMessage,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditClientScreen(
    uiState: AddEditClientUiState,
    onNameChange: (String) -> Unit,
    onClientTypeChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
    onMessageShown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val form: ClientFormState = uiState.formState

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            onMessageShown()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isExistingClient) "Edit Client" else "New Client",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OxfordBlue
                )
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) { CircularProgressIndicator() }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = OxfordBlue),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Box {
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .matchParentSize()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            GoldAccent,
                                            GoldAccent.copy(alpha = 0.7f)
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier.padding(start = 22.dp).padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Client type selector
                            Text(
                                "Client Type",
                                style = MaterialTheme.typography.labelLarge,
                                color = GoldAccent,
                                fontWeight = FontWeight.Bold
                            )
                            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                                val compactLayout = isCompactPhoneWidth(maxWidth)

                                val chipColors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color.White.copy(alpha = 0.05f),
                                    labelColor = Color.White.copy(alpha = 0.7f),
                                    selectedContainerColor = GoldAccent.copy(alpha = 0.2f),
                                    selectedLabelColor = GoldAccent,
                                    selectedLeadingIconColor = GoldAccent
                                )

                                if (compactLayout) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        FilterChip(
                                            selected = form.clientType == "PRIVATE",
                                            onClick = { onClientTypeChange("PRIVATE") },
                                            label = { Text("Private") },
                                            colors = chipColors
                                        )
                                        FilterChip(
                                            selected = form.clientType == "BUSINESS",
                                            onClick = { onClientTypeChange("BUSINESS") },
                                            label = { Text("Business") },
                                            colors = chipColors
                                        )
                                    }
                                } else {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        FilterChip(
                                            selected = form.clientType == "PRIVATE",
                                            onClick = { onClientTypeChange("PRIVATE") },
                                            label = { Text("Private") },
                                            colors = chipColors
                                        )
                                        FilterChip(
                                            selected = form.clientType == "BUSINESS",
                                            onClick = { onClientTypeChange("BUSINESS") },
                                            label = { Text("Business") },
                                            colors = chipColors
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = form.name,
                                onValueChange = onNameChange,
                                label = { Text("Name *", color = Color.White.copy(alpha = 0.7f)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                isError = form.errorMessage != null && form.name.isBlank()
                            )

                            OutlinedTextField(
                                value = form.address,
                                onValueChange = onAddressChange,
                                label = { Text("Address", color = Color.White.copy(alpha = 0.7f)) },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2
                            )

                            OutlinedTextField(
                                value = form.phoneNumber,
                                onValueChange = onPhoneChange,
                                label = { Text("Phone", color = Color.White.copy(alpha = 0.7f)) },
                                isError = form.phoneFormatError != null,
                                supportingText = { Text(form.phoneFormatError ?: "Format: 000-0000000", color = if (form.phoneFormatError != null) MaterialTheme.colorScheme.error else Color.White.copy(alpha = 0.5f)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                            )

                            OutlinedTextField(
                                value = form.email,
                                onValueChange = onEmailChange,
                                label = { Text("Email", color = Color.White.copy(alpha = 0.7f)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                            )

                            OutlinedTextField(
                                value = form.notes,
                                onValueChange = onNotesChange,
                                label = { Text("Notes", color = Color.White.copy(alpha = 0.7f)) },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3
                            )
                        }
                    }
                }

                if (form.errorMessage != null) {
                    Text(
                        text = form.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                Button(
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isSaving,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldAccent,
                        contentColor = OxfordBlue
                    )
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 8.dp).size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Text(
                        text = if (uiState.isExistingClient) "Save Changes" else "Add Client",
                        fontWeight = FontWeight.Bold
                    )
                }

                if (uiState.isExistingClient) {
                    Button(
                        onClick = onDelete,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Text("Delete Client", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

