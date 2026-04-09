package com.example.propaintersplastererspayment.feature.client.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.propaintersplastererspayment.ProPaintersApplication
import com.example.propaintersplastererspayment.feature.client.vm.AddEditClientUiState
import com.example.propaintersplastererspayment.feature.client.vm.AddEditClientViewModel
import com.example.propaintersplastererspayment.feature.client.vm.ClientFormState

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
                title = { Text(if (uiState.isExistingClient) "Edit Client" else "New Client") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) { CircularProgressIndicator() }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Client type selector
                Text("Client Type", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = form.clientType == "PRIVATE",
                        onClick = { onClientTypeChange("PRIVATE") },
                        label = { Text("Private") }
                    )
                    FilterChip(
                        selected = form.clientType == "BUSINESS",
                        onClick = { onClientTypeChange("BUSINESS") },
                        label = { Text("Business") }
                    )
                }

                OutlinedTextField(
                    value = form.name,
                    onValueChange = onNameChange,
                    label = { Text("Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = form.errorMessage != null && form.name.isBlank()
                )

                OutlinedTextField(
                    value = form.address,
                    onValueChange = onAddressChange,
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                OutlinedTextField(
                    value = form.phoneNumber,
                    onValueChange = onPhoneChange,
                    label = { Text("Phone") },
                    isError = form.phoneFormatError != null,
                    supportingText = { Text(form.phoneFormatError ?: "Format: 000-0000000") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                OutlinedTextField(
                    value = form.email,
                    onValueChange = onEmailChange,
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                OutlinedTextField(
                    value = form.notes,
                    onValueChange = onNotesChange,
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                if (form.errorMessage != null) {
                    Text(
                        text = form.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isSaving
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                    }
                    Text(if (uiState.isExistingClient) "Save Changes" else "Add Client")
                }

                if (uiState.isExistingClient) {
                    Button(
                        onClick = onDelete,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Text("Delete Client")
                    }
                }
            }
        }
    }
}

