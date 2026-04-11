package com.example.propaintersplastererspayment.feature.setup.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.propaintersplastererspayment.ProPaintersApplication
import com.example.propaintersplastererspayment.core.ui.isCompactPhoneWidth
import androidx.compose.ui.graphics.Color
import com.example.propaintersplastererspayment.ui.theme.GoldAccent
import com.example.propaintersplastererspayment.ui.theme.OxfordBlue
import com.example.propaintersplastererspayment.feature.settings.vm.SettingsUiState
import com.example.propaintersplastererspayment.feature.settings.vm.SettingsViewModel

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
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Welcome! Let's set up your business",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Fill in your business details below. These will appear on invoices and PDFs.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Business Info
                SetupSectionCard(title = "Business Information") {
                    OutlinedTextField(
                        value = form.businessName,
                        onValueChange = onBusinessNameChange,
                        label = { Text("Business Name *", color = Color.White.copy(alpha = 0.7f)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = form.address,
                        onValueChange = onAddressChange,
                        label = { Text("Business Address *", color = Color.White.copy(alpha = 0.7f)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }

                // Contact
                SetupSectionCard(title = "Contact Details") {
                    OutlinedTextField(
                        value = form.phoneNumber,
                        onValueChange = onPhoneNumberChange,
                        label = { Text("Phone Number *", color = Color.White.copy(alpha = 0.7f)) },
                        isError = form.phoneFormatError != null,
                        supportingText = {
                            Text(
                                text = form.phoneFormatError ?: "Format: 000-0000000",
                                color = if (form.phoneFormatError != null) MaterialTheme.colorScheme.error else Color.White.copy(alpha = 0.5f)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    OutlinedTextField(
                        value = form.email,
                        onValueChange = onEmailChange,
                        label = { Text("Email Address *", color = Color.White.copy(alpha = 0.7f)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                }

                // Invoice & Tax
                SetupSectionCard(title = "Invoice & Tax") {
                    OutlinedTextField(
                        value = form.gstNumber,
                        onValueChange = onGstNumberChange,
                        label = { Text("GST Number (optional)", color = Color.White.copy(alpha = 0.7f)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = form.defaultGstPercentText,
                        onValueChange = onDefaultGstPercentChange,
                        label = { Text("Default GST Rate (%)", color = Color.White.copy(alpha = 0.7f)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val compactLayout = isCompactPhoneWidth(maxWidth)

                        if (compactLayout) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Enable GST on new invoices by default",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Switch(
                                    checked = form.gstEnabledByDefault,
                                    onCheckedChange = onGstEnabledChange
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Enable GST on new invoices by default",
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Switch(
                                    checked = form.gstEnabledByDefault,
                                    onCheckedChange = onGstEnabledChange
                                )
                            }
                        }
                    }
                }

                // Banking
                SetupSectionCard(title = "Banking") {
                    OutlinedTextField(
                        value = form.bankAccountNumber,
                        onValueChange = onBankAccountNumberChange,
                        label = { Text("Bank Account Number *", color = Color.White.copy(alpha = 0.7f)) },
                        isError = form.bankAccountFormatError != null,
                        supportingText = {
                            Text(
                                text = form.bankAccountFormatError ?: "Format: 00-0000-0000000-00",
                                color = if (form.bankAccountFormatError != null) MaterialTheme.colorScheme.error else Color.White.copy(alpha = 0.5f)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                // Labour Rate
                SetupSectionCard(title = "Labour Rate") {
                    OutlinedTextField(
                        value = form.defaultLabourRateText,
                        onValueChange = onDefaultLabourRateChange,
                        label = { Text("Default Labour Rate ($/hr) *", color = Color.White.copy(alpha = 0.7f)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
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
                            color = OxfordBlue
                        )
                    }
                    Text(
                        text = "Save & Continue",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SetupSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
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
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent
                )
                content()
            }
        }
    }
}
