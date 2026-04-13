package com.example.propaintersplastererspayment.feature.client.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.propaintersplastererspayment.ProPaintersApplication
import com.example.propaintersplastererspayment.core.ui.isCompactPhoneWidth
import com.example.propaintersplastererspayment.feature.client.vm.AddEditClientUiState
import com.example.propaintersplastererspayment.feature.client.vm.AddEditClientViewModel
import com.example.propaintersplastererspayment.feature.client.vm.ClientFormState
import com.example.propaintersplastererspayment.ui.components.*
import com.example.propaintersplastererspayment.ui.theme.*

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
    onNameChange: (TextFieldValue) -> Unit,
    onClientTypeChange: (String) -> Unit,
    onAddressChange: (TextFieldValue) -> Unit,
    onPhoneChange: (TextFieldValue) -> Unit,
    onEmailChange: (TextFieldValue) -> Unit,
    onNotesChange: (TextFieldValue) -> Unit,
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
        containerColor = CharcoalBackground,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isExistingClient) "Edit Client" else "New Client",
                        color = OffWhite,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = IndustrialGold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CharcoalBackground
                )
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = IndustrialGold) }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(AppDimensions.screenPadding),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                IndustrialCard {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Client type selector
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "Client Type",
                                style = MaterialTheme.typography.labelLarge,
                                color = TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                                val compactLayout = isCompactPhoneWidth(maxWidth)

                                val chipColors = FilterChipDefaults.filterChipColors(
                                    containerColor = CharcoalMuted,
                                    labelColor = TextMuted,
                                    selectedContainerColor = IndustrialGold.copy(alpha = 0.2f),
                                    selectedLabelColor = IndustrialGold,
                                    selectedLeadingIconColor = IndustrialGold
                                )

                                if (compactLayout) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        FilterChip(
                                            selected = form.clientType == "PRIVATE",
                                            onClick = { onClientTypeChange("PRIVATE") },
                                            label = { Text("Private") },
                                            colors = chipColors,
                                            border = FilterChipDefaults.filterChipBorder(
                                                borderColor = BorderColor,
                                                selectedBorderColor = IndustrialGold,
                                                enabled = true,
                                                selected = form.clientType == "PRIVATE"
                                            )
                                        )
                                        FilterChip(
                                            selected = form.clientType == "BUSINESS",
                                            onClick = { onClientTypeChange("BUSINESS") },
                                            label = { Text("Business") },
                                            colors = chipColors,
                                            border = FilterChipDefaults.filterChipBorder(
                                                borderColor = BorderColor,
                                                selectedBorderColor = IndustrialGold,
                                                enabled = true,
                                                selected = form.clientType == "BUSINESS"
                                            )
                                        )
                                    }
                                } else {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        FilterChip(
                                            selected = form.clientType == "PRIVATE",
                                            onClick = { onClientTypeChange("PRIVATE") },
                                            label = { Text("Private") },
                                            colors = chipColors,
                                            border = FilterChipDefaults.filterChipBorder(
                                                borderColor = BorderColor,
                                                selectedBorderColor = IndustrialGold,
                                                enabled = true,
                                                selected = form.clientType == "PRIVATE"
                                            )
                                        )
                                        FilterChip(
                                            selected = form.clientType == "BUSINESS",
                                            onClick = { onClientTypeChange("BUSINESS") },
                                            label = { Text("Business") },
                                            colors = chipColors,
                                            border = FilterChipDefaults.filterChipBorder(
                                                borderColor = BorderColor,
                                                selectedBorderColor = IndustrialGold,
                                                enabled = true,
                                                selected = form.clientType == "BUSINESS"
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        IndustrialTextField(
                            value = form.name,
                            onValueChange = onNameChange,
                            label = "Name *",
                            placeholder = "Enter client name",
                            singleLine = true
                        )

                        IndustrialTextField(
                            value = form.address,
                            onValueChange = onAddressChange,
                            label = "Address",
                            placeholder = "Enter client address",
                            singleLine = false
                        )

                        IndustrialTextField(
                            value = form.phoneNumber,
                            onValueChange = onPhoneChange,
                            label = "Phone",
                            placeholder = "000-0000000",
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                        if (form.phoneFormatError != null) {
                            Text(
                                text = form.phoneFormatError ?: "",
                                color = ErrorRed,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        IndustrialTextField(
                            value = form.email,
                            onValueChange = onEmailChange,
                            label = "Email",
                            placeholder = "example@email.com",
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )

                        IndustrialTextField(
                            value = form.notes,
                            onValueChange = onNotesChange,
                            label = "Notes",
                            placeholder = "Additional information...",
                            singleLine = false
                        )
                    }
                }

                if (form.errorMessage != null) {
                    Text(
                        text = form.errorMessage,
                        color = ErrorRed,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                PrimaryButton(
                    text = if (uiState.isExistingClient) "Save Changes" else "Add Client",
                    onClick = onSave,
                    enabled = !uiState.isSaving,
                    icon = {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = CharcoalBackground
                            )
                        } else {
                            Icon(Icons.Default.Save, null)
                        }
                    }
                )

                if (uiState.isExistingClient) {
                    SecondaryButton(
                        text = "Delete Client",
                        onClick = onDelete,
                        icon = { Icon(Icons.Default.Delete, null, tint = ErrorRed) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

