package com.example.propaintersplastererspayment.feature.setup.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.propaintersplastererspayment.ProPaintersApplication
import com.example.propaintersplastererspayment.feature.settings.vm.SettingsUiState
import com.example.propaintersplastererspayment.feature.settings.vm.SettingsViewModel

/**
 * First-run screen that forces the user to set up their business details
 * before they can access the main app. Cannot be skipped.
 *
 * Reuses [SettingsViewModel] – once settings are saved the [settingsSaved]
 * SharedFlow triggers navigation to [HomeScreen].
 */
@Composable
fun InitialSetupRoute(
    onSetupComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val application = LocalContext.current.applicationContext as ProPaintersApplication
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.provideFactory(application.container.settingsRepository)
    )
    val uiState by viewModel.uiState.collectAsState()

    // Navigate to Home as soon as settings are saved
    LaunchedEffect(Unit) {
        viewModel.settingsSaved.collect { onSetupComplete() }
    }

    InitialSetupScreen(
        uiState = uiState,
        onBusinessNameChange = viewModel::onBusinessNameChange,
        onAddressChange = viewModel::onAddressChange,
        onPhoneNumberChange = viewModel::onPhoneNumberChange,
        onEmailChange = viewModel::onEmailChange,
        onGstNumberChange = viewModel::onGstNumberChange,
        onBankAccountNumberChange = viewModel::onBankAccountNumberChange,
        onInvoicePrefixChange = viewModel::onInvoicePrefixChange,
        onDefaultLabourRateChange = viewModel::onDefaultLabourRateChange,
        onDefaultGstPercentChange = viewModel::onDefaultGstPercentChange,
        onGstEnabledChange = viewModel::onGstEnabledChange,
        onSave = viewModel::saveSettings,
        onMessageShown = viewModel::clearUserMessage,
        modifier = modifier
    )
}

@Composable
fun InitialSetupScreen(
    uiState: SettingsUiState,
    onBusinessNameChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onGstNumberChange: (String) -> Unit,
    onBankAccountNumberChange: (String) -> Unit,
    onInvoicePrefixChange: (String) -> Unit,
    onDefaultLabourRateChange: (String) -> Unit,
    onDefaultGstPercentChange: (String) -> Unit,
    onGstEnabledChange: (Boolean) -> Unit,
    onSave: () -> Unit,
    onMessageShown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val form = uiState.formState

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            onMessageShown()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
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
                // Welcome header
                Text(
                    text = "Welcome! Let's set up your business",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Fill in your business details below. These will appear on invoices and PDFs.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )

                // Business Info
                SectionLabel("Business Information")
                OutlinedTextField(
                    value = form.businessName,
                    onValueChange = onBusinessNameChange,
                    label = { Text("Business Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = form.address,
                    onValueChange = onAddressChange,
                    label = { Text("Business Address *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                // Contact
                SectionLabel("Contact Details")
                OutlinedTextField(
                    value = form.phoneNumber,
                    onValueChange = onPhoneNumberChange,
                    label = { Text("Phone Number *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                OutlinedTextField(
                    value = form.email,
                    onValueChange = onEmailChange,
                    label = { Text("Email Address *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                // Invoice & Tax
                SectionLabel("Invoice & Tax")
                OutlinedTextField(
                    value = form.gstNumber,
                    onValueChange = onGstNumberChange,
                    label = { Text("GST Number (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = form.invoiceNumberPrefix,
                    onValueChange = onInvoicePrefixChange,
                    label = { Text("Invoice Number Prefix *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = { Text("e.g. INV, PP, INVOICE") }
                )
                OutlinedTextField(
                    value = form.defaultGstPercentText,
                    onValueChange = onDefaultGstPercentChange,
                    label = { Text("Default GST Rate (%)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Enable GST on new invoices by default")
                    Switch(
                        checked = form.gstEnabledByDefault,
                        onCheckedChange = onGstEnabledChange
                    )
                }

                // Banking
                SectionLabel("Banking")
                OutlinedTextField(
                    value = form.bankAccountNumber,
                    onValueChange = onBankAccountNumberChange,
                    label = { Text("Bank Account Number *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // Labour Rate
                SectionLabel("Labour Rate")
                OutlinedTextField(
                    value = form.defaultLabourRateText,
                    onValueChange = onDefaultLabourRateChange,
                    label = { Text("Default Labour Rate ($/hr) *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
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
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    enabled = !uiState.isSaving
                ) {
                    if (uiState.isSaving) CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                    Text("Save & Continue")
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp)
    )
}
