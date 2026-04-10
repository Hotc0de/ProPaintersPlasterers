package com.example.propaintersplastererspayment.feature.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.propaintersplastererspayment.ProPaintersApplication
import com.example.propaintersplastererspayment.R
import com.example.propaintersplastererspayment.core.ui.isCompactPhoneWidth
import com.example.propaintersplastererspayment.core.util.CurrencyFormatUtils
import com.example.propaintersplastererspayment.data.local.entity.AppSettingsEntity
import com.example.propaintersplastererspayment.feature.settings.vm.SettingsFormState
import com.example.propaintersplastererspayment.feature.settings.vm.SettingsUiState
import com.example.propaintersplastererspayment.feature.settings.vm.SettingsViewModel
import com.example.propaintersplastererspayment.ui.theme.ProPaintersPlasterersPaymentTheme

// ─────────────────────────────────────────────────────────────────────────────
// Route — wires the ViewModel to the screen
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Entry point composable for the Settings feature.
 * Resolves the [SettingsViewModel] from the app container and passes all actions
 * down to the stateless [SettingsScreen].
 */
@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val application = LocalContext.current.applicationContext as ProPaintersApplication
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.provideFactory(
            settingsRepository = application.container.settingsRepository
        )
    )
    val uiState by viewModel.uiState.collectAsState()

    SettingsScreen(
        uiState = uiState,
        modifier = modifier,
        onBack = onBack,
        onBusinessNameChange = viewModel::onBusinessNameChange,
        onAddressChange = viewModel::onAddressChange,
        onPhoneNumberChange = viewModel::onPhoneNumberChange,
        onEmailChange = viewModel::onEmailChange,
        onGstNumberChange = viewModel::onGstNumberChange,
        onBankAccountNumberChange = viewModel::onBankAccountNumberChange,
        onDefaultLabourRateChange = viewModel::onDefaultLabourRateChange,
        onDefaultGstPercentChange = viewModel::onDefaultGstPercentChange,
        onGstEnabledChange = viewModel::onGstEnabledChange,
        onSave = viewModel::saveSettings,
        onMessageShown = viewModel::clearUserMessage
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Main stateless screen
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Stateless settings screen. All state comes from [uiState]; actions are lambdas.
 *
 * Layout:
 *  1. Business Info section
 *  2. Contact section
 *  3. Invoice & Tax section
 *  4. Banking section
 *  5. Save button + optional "Last saved at" timestamp
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onBack: () -> Unit,
    onBusinessNameChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onGstNumberChange: (String) -> Unit,
    onBankAccountNumberChange: (String) -> Unit,
    onDefaultLabourRateChange: (String) -> Unit,
    onDefaultGstPercentChange: (String) -> Unit,
    onGstEnabledChange: (Boolean) -> Unit,
    onSave: () -> Unit,
    onMessageShown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onMessageShown()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // ── Header ─────────────────────────────────────────
                        Text(
                            text = stringResource(R.string.settings_title),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.settings_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )

                        // ── Business Info ──────────────────────────────────
                        SectionHeader(text = stringResource(R.string.settings_section_business))
                        OutlinedTextField(
                            value = uiState.formState.businessName,
                            onValueChange = onBusinessNameChange,
                            label = { Text(stringResource(R.string.settings_business_name)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = uiState.formState.address,
                            onValueChange = onAddressChange,
                            label = { Text(stringResource(R.string.settings_address)) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )

                        // ── Contact ────────────────────────────────────────
                        SectionHeader(text = stringResource(R.string.settings_section_contact))
                        OutlinedTextField(
                            value = uiState.formState.phoneNumber,
                            onValueChange = onPhoneNumberChange,
                            label = { Text(stringResource(R.string.settings_phone)) },
                            isError = uiState.formState.phoneFormatError != null,
                            supportingText = {
                                Text(uiState.formState.phoneFormatError ?: "Format: 000-0000000")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                        OutlinedTextField(
                            value = uiState.formState.email,
                            onValueChange = onEmailChange,
                            label = { Text(stringResource(R.string.settings_email)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )

                        // ── Invoice & Tax ──────────────────────────────────
                        SectionHeader(text = stringResource(R.string.settings_section_invoice))
                        OutlinedTextField(
                            value = uiState.formState.gstNumber,
                            onValueChange = onGstNumberChange,
                            label = { Text(stringResource(R.string.settings_gst_number)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = uiState.formState.defaultLabourRateText,
                            onValueChange = onDefaultLabourRateChange,
                            label = { Text(stringResource(R.string.settings_labour_rate)) },
                            supportingText = { Text(stringResource(R.string.settings_labour_rate_hint)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = uiState.formState.defaultGstPercentText,
                            onValueChange = onDefaultGstPercentChange,
                            label = { Text(stringResource(R.string.settings_gst_percent)) },
                            supportingText = { Text(stringResource(R.string.settings_gst_percent_hint)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        // Labour rate preview
                        if (uiState.formState.parsedLabourRate != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "${stringResource(R.string.settings_labour_rate_preview)}: ${
                                        CurrencyFormatUtils.formatCurrency(uiState.formState.parsedLabourRate!!)
                                    }",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        // GST toggle
                        Card(modifier = Modifier.fillMaxWidth()) {
                            BoxWithConstraints(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                val compactLayout = isCompactPhoneWidth(maxWidth)

                                if (compactLayout) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = stringResource(R.string.settings_gst_enabled),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = stringResource(R.string.settings_gst_enabled_hint),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                        Switch(
                                            checked = uiState.formState.gstEnabledByDefault,
                                            onCheckedChange = onGstEnabledChange
                                        )
                                    }
                                } else {
                                    androidx.compose.foundation.layout.Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = stringResource(R.string.settings_gst_enabled),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = stringResource(R.string.settings_gst_enabled_hint),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                        Switch(
                                            checked = uiState.formState.gstEnabledByDefault,
                                            onCheckedChange = onGstEnabledChange
                                        )
                                    }
                                }
                            }
                        }

                        // ── Banking ────────────────────────────────────────
                        SectionHeader(text = stringResource(R.string.settings_section_banking))
                        OutlinedTextField(
                            value = uiState.formState.bankAccountNumber,
                            onValueChange = onBankAccountNumberChange,
                            label = { Text(stringResource(R.string.settings_bank_account)) },
                            isError = uiState.formState.bankAccountFormatError != null,
                            supportingText = {
                                Text(uiState.formState.bankAccountFormatError ?: "Format: 00-0000-0000000-00")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // ── Validation errors ──────────────────────────────
                        uiState.formState.errorMessage?.let { error ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                Text(
                                    text = error,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        // ── Save button & status ───────────────────────────
                        Button(
                            onClick = onSave,
                            enabled = !uiState.isSaving && uiState.formState.isValid,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.padding(end = 8.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                            Text(
                                text = stringResource(R.string.settings_save),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        // Last saved timestamp
                        if (uiState.lastSavedAt != null) {
                            Text(
                                text = "${stringResource(R.string.settings_last_saved)} ${uiState.lastSavedAt}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }

                        // Bottom padding
                        androidx.compose.foundation.layout.Spacer(
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sub-composables
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Section header with a clean style.
 */
@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp)
    )
}



// ─────────────────────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    ProPaintersPlasterersPaymentTheme {
        SettingsScreen(
            uiState = SettingsUiState(
                isLoading = false,
                settings = AppSettingsEntity(
                    businessName = "Pro Painters & Plasterers",
                    address = "123 Main St, Sydney NSW 2000",
                    phoneNumber = "02 9123 4567",
                    email = "info@propainters.com.au",
                    gstNumber = "12 345 678 901",
                    bankAccountNumber = "12-1234-1234567-12",
                    defaultLabourRate = 65.0,
                    defaultGstRate = 0.15,
                    gstEnabledByDefault = true
                ),
                formState = SettingsFormState(
                    businessName = "Pro Painters & Plasterers",
                    address = "123 Main St, Sydney NSW 2000",
                    phoneNumber = "02 9123 4567",
                    email = "info@propainters.com.au",
                    gstNumber = "12 345 678 901",
                    bankAccountNumber = "12-1234-1234567-12",
                    defaultLabourRateText = "65",
                    defaultGstPercentText = "15",
                    gstEnabledByDefault = true
                ),
                lastSavedAt = "14:32"
            ),
            onBack = {},
            onBusinessNameChange = {},
            onAddressChange = {},
            onPhoneNumberChange = {},
            onEmailChange = {},
            onGstNumberChange = {},
            onBankAccountNumberChange = {},
            onDefaultLabourRateChange = {},
            onDefaultGstPercentChange = {},
            onGstEnabledChange = {},
            onSave = {},
            onMessageShown = {}
        )
    }
}



