package com.example.propaintersplastererspayment.feature.job.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.propaintersplastererspayment.ProPaintersApplication
import com.example.propaintersplastererspayment.R
import com.example.propaintersplastererspayment.data.local.entity.ClientEntity
import com.example.propaintersplastererspayment.feature.job.vm.JobFormUiState
import com.example.propaintersplastererspayment.feature.job.vm.JobFormViewModel

@Composable
fun JobFormRoute(
    jobId: Long?,
    newClientId: Long?,
    onConsumeNewClientId: () -> Unit,
    onAddNewClient: () -> Unit,
    onBack: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val application = LocalContext.current.applicationContext as ProPaintersApplication
    val viewModel: JobFormViewModel = viewModel(
        factory = JobFormViewModel.provideFactory(
            jobId = jobId,
            jobRepository = application.container.jobRepository,
            clientRepository = application.container.clientRepository
        )
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) onDone()
    }

    LaunchedEffect(newClientId) {
        if (newClientId != null && newClientId > 0L) {
            viewModel.onClientAdded(newClientId)
            onConsumeNewClientId()
        }
    }

    JobFormScreen(
        uiState = uiState,
        onAddressChange = viewModel::onAddressChange,
        onClientQueryChange = viewModel::onClientQueryChange,
        onClientSelected = viewModel::onClientSelected,
        onAddNewClient = onAddNewClient,
        onBack = onBack,
        onNotesChange = viewModel::onNotesChange,
        onSave = viewModel::saveJob,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobFormScreen(
    uiState: JobFormUiState,
    onAddressChange: (String) -> Unit,
    onClientQueryChange: (String) -> Unit,
    onClientSelected: (ClientEntity) -> Unit,
    onAddNewClient: () -> Unit,
    onBack: () -> Unit,
    onNotesChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    var clientDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.jobId == null) {
                            stringResource(R.string.job_add_title)
                        } else {
                            stringResource(R.string.job_edit_title)
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = uiState.address,
                    onValueChange = onAddressChange,
                    label = { Text(stringResource(R.string.job_address)) },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = clientDropdownExpanded,
                    onExpandedChange = { clientDropdownExpanded = !clientDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = uiState.clientQuery,
                        onValueChange = {
                            onClientQueryChange(it)
                            clientDropdownExpanded = true
                        },
                        label = { Text(stringResource(R.string.job_client_name)) },
                        placeholder = { Text(stringResource(R.string.job_client_search_hint)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(
                                type = ExposedDropdownMenuAnchorType.PrimaryEditable,
                                enabled = true
                            ),
                        singleLine = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = clientDropdownExpanded)
                        }
                    )

                    ExposedDropdownMenu(
                        expanded = clientDropdownExpanded,
                        onDismissRequest = { clientDropdownExpanded = false }
                    ) {
                        uiState.filteredClients.take(20).forEach { client ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(client.name, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            text = client.clientType,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                },
                                onClick = {
                                    onClientSelected(client)
                                    clientDropdownExpanded = false
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = {
                                TextButton(onClick = {
                                    clientDropdownExpanded = false
                                    onAddNewClient()
                                }) {
                                    androidx.compose.material3.Icon(
                                        Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text(stringResource(R.string.job_add_new_client))
                                }
                            },
                            onClick = {
                                clientDropdownExpanded = false
                                onAddNewClient()
                            }
                        )
                    }
                }

                if (uiState.selectedClientName.isNotBlank()) {
                    Text(
                        text = stringResource(R.string.job_selected_client, uiState.selectedClientName),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            clientDropdownExpanded = true
                        }
                    )
                }

                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = onNotesChange,
                    label = { Text(stringResource(R.string.job_notes)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                uiState.errorMessage?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Button(
                    onClick = onSave,
                    enabled = !uiState.isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp), strokeWidth = 2.dp)
                    }
                    Text(text = stringResource(R.string.job_save))
                }
            }
        }
    }
}

