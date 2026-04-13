package com.example.propaintersplastererspayment.feature.setup.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.propaintersplastererspayment.ProPaintersApplication
import com.example.propaintersplastererspayment.R
import com.example.propaintersplastererspayment.feature.settings.vm.SettingsUiState
import com.example.propaintersplastererspayment.feature.settings.vm.SettingsViewModel
import com.example.propaintersplastererspayment.ui.components.IndustrialCard
import com.example.propaintersplastererspayment.ui.components.IndustrialTextField
import com.example.propaintersplastererspayment.ui.components.PrimaryButton
import com.example.propaintersplastererspayment.ui.theme.AppDimensions
import com.example.propaintersplastererspayment.ui.theme.CharcoalBackground
import com.example.propaintersplastererspayment.ui.theme.ErrorRed
import com.example.propaintersplastererspayment.ui.theme.IndustrialGold
import com.example.propaintersplastererspayment.ui.theme.OffWhite
import com.example.propaintersplastererspayment.ui.theme.TextMuted

/**
 * First-run screen that forces the user to set up their business details
 * before they can access the main app. Cannot be skipped.
 *
 * Reuses SettingsViewModel and navigates to Home after settings are saved.
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
    onBusinessNameChange: (TextFieldValue) -> Unit,
    onAddressChange: (TextFieldValue) -> Unit,
    onPhoneNumberChange: (TextFieldValue) -> Unit,
    onEmailChange: (TextFieldValue) -> Unit,
    onGstNumberChange: (TextFieldValue) -> Unit,
    onBankAccountNumberChange: (TextFieldValue) -> Unit,
    onDefaultLabourRateChange: (TextFieldValue) -> Unit,
    onDefaultGstPercentChange: (TextFieldValue) -> Unit,
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
        containerColor = CharcoalBackground,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
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
                // Welcome header
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Welcome! Let's set up your business",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = IndustrialGold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Fill in your business details below. These will appear on invoices and PDFs.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Business Info
                SetupSectionCard(title = "Business Information") {
                    IndustrialTextField(
                        value = form.businessName,
                        onValueChange = onBusinessNameChange,
                        label = "Business Name *",
                        placeholder = "Enter business name",
                        singleLine = true
                    )
                    IndustrialTextField(
                        value = form.address,
                        onValueChange = onAddressChange,
                        label = "Business Address *",
                        placeholder = "Enter business address",
                        singleLine = false
                    )
                }

                // Contact
                SetupSectionCard(title = "Contact Details") {
                    IndustrialTextField(
                        value = form.phoneNumber,
                        onValueChange = onPhoneNumberChange,
                        label = "Phone Number *",
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
                        label = "Email Address *",
                        placeholder = "business@email.com",
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                }

                // Invoice & Tax
                SetupSectionCard(title = "Invoice & Tax") {
                    IndustrialTextField(
                        value = form.gstNumber,
                        onValueChange = onGstNumberChange,
                        label = "GST Number (optional)",
                        placeholder = "Enter GST number",
                        singleLine = true
                    )
                    IndustrialTextField(
                        value = form.defaultGstPercentText,
                        onValueChange = onDefaultGstPercentChange,
                        label = "Default GST Rate (%)",
                        placeholder = "15",
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Enable GST by default",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = OffWhite
                            )
                            Text(
                                text = "New invoices will have GST enabled",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                        Switch(
                            checked = form.gstEnabledByDefault,
                            onCheckedChange = onGstEnabledChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = IndustrialGold,
                                checkedTrackColor = IndustrialGold.copy(alpha = 0.5f)
                            )
                        )
                    }
                }

                // Banking
                SetupSectionCard(title = "Banking") {
                    IndustrialTextField(
                        value = form.bankAccountNumber,
                        onValueChange = onBankAccountNumberChange,
                        label = "Bank Account Number *",
                        placeholder = "00-0000-0000000-00",
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    if (form.bankAccountFormatError != null) {
                        Text(
                            text = form.bankAccountFormatError ?: "",
                            color = ErrorRed,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // Labour Rate
                SetupSectionCard(title = "Labour Rate") {
                    IndustrialTextField(
                        value = form.defaultLabourRateText,
                        onValueChange = onDefaultLabourRateChange,
                        label = "Default Labour Rate ($/hr) *",
                        placeholder = "0.00",
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                if (form.errorMessage != null) {
                    Text(
                        text = form.errorMessage ?: "",
                        color = ErrorRed,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                PrimaryButton(
                    text = "Save & Continue",
                    onClick = onSave,
                    enabled = !uiState.isSaving && form.isValid,
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

@Composable
private fun SetupSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    IndustrialCard {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = IndustrialGold
            )
            content()
        }
    }
}
