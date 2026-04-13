package com.example.propaintersplastererspayment.feature.job.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.propaintersplastererspayment.ProPaintersApplication
import com.example.propaintersplastererspayment.R
import com.example.propaintersplastererspayment.data.local.entity.ClientEntity
import com.example.propaintersplastererspayment.feature.job.vm.JobFormUiState
import com.example.propaintersplastererspayment.feature.job.vm.JobFormViewModel
import com.example.propaintersplastererspayment.ui.components.IndustrialCard
import com.example.propaintersplastererspayment.ui.components.IndustrialTextField
import com.example.propaintersplastererspayment.ui.components.PrimaryButton
import com.example.propaintersplastererspayment.ui.theme.AppDimensions
import com.example.propaintersplastererspayment.ui.theme.AppShapes
import com.example.propaintersplastererspayment.ui.theme.CharcoalBackground
import com.example.propaintersplastererspayment.ui.theme.ErrorRed
import com.example.propaintersplastererspayment.ui.theme.IndustrialGold
import com.example.propaintersplastererspayment.ui.theme.OffWhite
import com.example.propaintersplastererspayment.ui.theme.TextMuted
import com.example.propaintersplastererspayment.ui.theme.TextSubdued

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
    onAddressChange: (TextFieldValue) -> Unit,
    onClientQueryChange: (TextFieldValue) -> Unit,
    onClientSelected: (ClientEntity) -> Unit,
    onAddNewClient: () -> Unit,
    onBack: () -> Unit,
    onNotesChange: (TextFieldValue) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    var clientDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CharcoalBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.jobId == null) {
                            stringResource(R.string.job_add_title)
                        } else {
                            stringResource(R.string.job_edit_title)
                        },
                        color = OffWhite,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = IndustrialGold)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(AppDimensions.screenPadding),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                IndustrialCard {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        IndustrialTextField(
                            value = uiState.address,
                            onValueChange = onAddressChange,
                            label = stringResource(R.string.job_address),
                            placeholder = "Enter site address",
                            singleLine = true
                        )

                        Column {
                            Text(
                                text = stringResource(R.string.job_client_name),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            ExposedDropdownMenuBox(
                                expanded = clientDropdownExpanded,
                                onExpandedChange = { clientDropdownExpanded = !clientDropdownExpanded }
                            ) {
                                androidx.compose.material3.OutlinedTextField(
                                    value = uiState.clientQuery,
                                    onValueChange = {
                                        onClientQueryChange(it)
                                        clientDropdownExpanded = true
                                    },
                                    placeholder = { 
                                        Text(
                                            stringResource(R.string.job_client_search_hint), 
                                            color = TextSubdued,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        ) 
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(
                                            type = ExposedDropdownMenuAnchorType.PrimaryEditable,
                                            enabled = true
                                        ),
                                    singleLine = true,
                                    shape = AppShapes.large,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = OffWhite,
                                        unfocusedTextColor = OffWhite,
                                        focusedBorderColor = IndustrialGold,
                                        unfocusedBorderColor = IndustrialGold.copy(alpha = 0.5f),
                                        cursorColor = IndustrialGold
                                    ),
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = clientDropdownExpanded)
                                    }
                                )

                                ExposedDropdownMenu(
                                    expanded = clientDropdownExpanded,
                                    onDismissRequest = { clientDropdownExpanded = false },
                                    modifier = Modifier.background(CharcoalBackground)
                                ) {
                                    uiState.filteredClients.take(20).forEach { client ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(client.name, fontWeight = FontWeight.SemiBold, color = OffWhite)
                                                    Text(
                                                        text = client.clientType,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = TextMuted
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
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp),
                                                    tint = IndustrialGold
                                                )
                                                Text(
                                                    text = stringResource(R.string.job_add_new_client),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    color = IndustrialGold
                                                )
                                            }
                                        },
                                        onClick = {
                                            clientDropdownExpanded = false
                                            onAddNewClient()
                                        }
                                    )
                                }
                            }
                        }

                        if (uiState.selectedClientName.isNotBlank()) {
                            Text(
                                text = stringResource(R.string.job_selected_client, uiState.selectedClientName),
                                style = MaterialTheme.typography.labelMedium,
                                color = IndustrialGold,
                                modifier = Modifier.clickable {
                                    clientDropdownExpanded = true
                                }
                            )
                        }

                        IndustrialTextField(
                            value = uiState.notes,
                            onValueChange = onNotesChange,
                            label = stringResource(R.string.job_notes),
                            placeholder = "Enter job notes or instructions",
                            singleLine = false
                        )
                    }
                }

                uiState.errorMessage?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ErrorRed,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                PrimaryButton(
                    text = stringResource(R.string.job_save),
                    onClick = onSave,
                    enabled = !uiState.isSaving,
                    icon = {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = CharcoalBackground
                            )
                        }
                    }
                )
            }
        }
    }
}
