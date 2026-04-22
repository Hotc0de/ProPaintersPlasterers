package com.example.propaintersplastererspayment.feature.settings.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import com.example.propaintersplastererspayment.ui.components.*
import com.example.propaintersplastererspayment.ui.theme.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
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
import com.example.propaintersplastererspayment.ui.theme.ProPaintersTheme

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
        onBankNameChange = viewModel::onBankNameChange,
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
    onBusinessNameChange: (TextFieldValue) -> Unit,
    onAddressChange: (TextFieldValue) -> Unit,
    onPhoneNumberChange: (TextFieldValue) -> Unit,
    onEmailChange: (TextFieldValue) -> Unit,
    onGstNumberChange: (TextFieldValue) -> Unit,
    onBankAccountNumberChange: (TextFieldValue) -> Unit,
    onBankNameChange: (TextFieldValue) -> Unit,
    onDefaultLabourRateChange: (TextFieldValue) -> Unit,
    onDefaultGstPercentChange: (TextFieldValue) -> Unit,
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
        containerColor = CharcoalBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
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
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = IndustrialGold)
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(AppDimensions.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.settings_title),
                            style = MaterialTheme.typography.headlineSmall,
                            color = IndustrialGold,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.settings_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    }

                    // ── Business Info Section ────────────────────────────
                    SettingsSection(title = stringResource(R.string.settings_section_business)) {
                        IndustrialTextField(
                            value = uiState.formState.businessName,
                            onValueChange = onBusinessNameChange,
                            label = stringResource(R.string.settings_business_name),
                            placeholder = "Enter business name",
                            singleLine = true
                        )
                        IndustrialTextField(
                            value = uiState.formState.address,
                            onValueChange = onAddressChange,
                            label = stringResource(R.string.settings_address),
                            placeholder = "Enter business address",
                            singleLine = false
                        )
                    }

                    // ── Contact Section ──────────────────────────────────
                    SettingsSection(title = stringResource(R.string.settings_section_contact)) {
                        IndustrialTextField(
                            value = uiState.formState.phoneNumber,
                            onValueChange = onPhoneNumberChange,
                            label = stringResource(R.string.settings_phone),
                            placeholder = "000-0000000",
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                        if (uiState.formState.phoneFormatError != null) {
                            Text(
                                text = uiState.formState.phoneFormatError ?: "",
                                color = ErrorRed,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        IndustrialTextField(
                            value = uiState.formState.email,
                            onValueChange = onEmailChange,
                            label = stringResource(R.string.settings_email),
                            placeholder = "business@email.com",
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                    }

                    // ── Invoice & Tax Section ────────────────────────────
                    SettingsSection(title = stringResource(R.string.settings_section_invoice)) {
                        IndustrialTextField(
                            value = uiState.formState.gstNumber,
                            onValueChange = onGstNumberChange,
                            label = stringResource(R.string.settings_gst_number),
                            placeholder = "Enter GST number",
                            singleLine = true
                        )
                        IndustrialTextField(
                            value = uiState.formState.defaultLabourRateText,
                            onValueChange = onDefaultLabourRateChange,
                            label = stringResource(R.string.settings_labour_rate),
                            placeholder = "0.00",
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        IndustrialTextField(
                            value = uiState.formState.defaultGstPercentText,
                            onValueChange = onDefaultGstPercentChange,
                            label = stringResource(R.string.settings_gst_percent),
                            placeholder = "15",
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        
                        // Labour rate preview
                        if (uiState.formState.parsedLabourRate != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = AppShapes.medium,
                                colors = CardDefaults.cardColors(containerColor = CharcoalMuted),
                                border = BorderStroke(1.dp, BorderColor)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Info, null, tint = IndustrialGold, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "${stringResource(R.string.settings_labour_rate_preview)}: ${
                                            CurrencyFormatUtils.formatCurrency(uiState.formState.parsedLabourRate!!)
                                        }",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = OffWhite
                                    )
                                }
                            }
                        }

                        // GST toggle
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = AppShapes.medium,
                            colors = CardDefaults.cardColors(containerColor = CharcoalMuted),
                            border = BorderStroke(1.dp, BorderColor)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.settings_gst_enabled),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = OffWhite
                                    )
                                    Text(
                                        text = stringResource(R.string.settings_gst_enabled_hint),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }
                                Switch(
                                    checked = uiState.formState.gstEnabledByDefault,
                                    onCheckedChange = onGstEnabledChange,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = IndustrialGold,
                                        checkedTrackColor = IndustrialGold.copy(alpha = 0.5f)
                                    )
                                )
                            }
                        }
                    }

                    // ── Banking Section ──────────────────────────────────
                    SettingsSection(title = stringResource(R.string.settings_section_banking)) {
                        IndustrialTextField(
                            value = uiState.formState.bankName,
                            onValueChange = onBankNameChange,
                            label = "Bank Name",
                            placeholder = "e.g. ANZ Bank",
                            singleLine = true
                        )
                        IndustrialTextField(
                            value = uiState.formState.bankAccountNumber,
                            onValueChange = onBankAccountNumberChange,
                            label = stringResource(R.string.settings_bank_account),
                            placeholder = "00-0000-0000000-00",
                            singleLine = true
                        )
                        if (uiState.formState.bankAccountFormatError != null) {
                            Text(
                                text = uiState.formState.bankAccountFormatError ?: "",
                                color = ErrorRed,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    // ── Validation errors ──────────────────────────────
                    uiState.formState.errorMessage?.let { error ->
                        Text(
                            text = error,
                            color = ErrorRed,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    // ── Save button & status ───────────────────────────
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        PrimaryButton(
                            text = stringResource(R.string.settings_save),
                            onClick = onSave,
                            enabled = !uiState.isSaving && uiState.formState.isValid,
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

                        // Last saved timestamp
                        if (uiState.lastSavedAt != null) {
                            Text(
                                text = "${stringResource(R.string.settings_last_saved)} ${uiState.lastSavedAt}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sub-composables
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SettingsSection(
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

// ─────────────────────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    ProPaintersTheme {
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
                    businessName = TextFieldValue("Pro Painters & Plasterers"),
                    address = TextFieldValue("123 Main St, Sydney NSW 2000"),
                    phoneNumber = TextFieldValue("02 9123 4567"),
                    email = TextFieldValue("info@propainters.com.au"),
                    gstNumber = TextFieldValue("12 345 678 901"),
                    bankAccountNumber = TextFieldValue("12-1234-1234567-12"),
                    defaultLabourRateText = TextFieldValue("65"),
                    defaultGstPercentText = TextFieldValue("15"),
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
            onBankNameChange = {},
            onDefaultLabourRateChange = {},
            onDefaultGstPercentChange = {},
            onGstEnabledChange = {},
            onSave = {},
            onMessageShown = {}
        )
    }
}



