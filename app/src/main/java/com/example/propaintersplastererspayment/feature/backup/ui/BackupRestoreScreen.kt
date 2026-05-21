package com.example.propaintersplastererspayment.feature.backup.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.propaintersplastererspayment.ProPaintersApplication
import com.example.propaintersplastererspayment.data.backup.BackupFileInfo
import com.example.propaintersplastererspayment.feature.backup.vm.BackupRestoreUiState
import com.example.propaintersplastererspayment.feature.backup.vm.BackupRestoreViewModel
import com.example.propaintersplastererspayment.ui.components.IndustrialCard
import com.example.propaintersplastererspayment.ui.components.PrimaryButton
import com.example.propaintersplastererspayment.ui.components.SecondaryButton
import com.example.propaintersplastererspayment.ui.theme.AppDimensions
import com.example.propaintersplastererspayment.ui.theme.CharcoalBackground
import com.example.propaintersplastererspayment.ui.theme.CharcoalMuted
import com.example.propaintersplastererspayment.ui.theme.CharcoalSecondary
import com.example.propaintersplastererspayment.ui.theme.IndustrialGold
import com.example.propaintersplastererspayment.ui.theme.OffWhite
import com.example.propaintersplastererspayment.ui.theme.TextMuted
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BackupRestoreRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val application = LocalContext.current.applicationContext as ProPaintersApplication
    val contentResolver = LocalContext.current.contentResolver
    val viewModel: BackupRestoreViewModel = viewModel(
        factory = BackupRestoreViewModel.provideFactory(application.container.backupRestoreService)
    )
    val uiState by viewModel.uiState.collectAsState()

    BackupRestoreScreen(
        uiState = uiState,
        onBack = onBack,
        onBackupSelected = { uri -> viewModel.exportBackup(contentResolver, uri) },
        onRestoreSelected = { uri -> viewModel.prepareRestore(contentResolver, uri) },
        onConfirmRestore = { viewModel.confirmRestore(contentResolver) },
        onCancelRestore = viewModel::cancelRestore,
        onMessageShown = viewModel::clearUserMessage,
        modifier = modifier
    )
}

@Composable
fun StartupChoiceRoute(
    onStartNew: () -> Unit,
    onRestoreComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val application = LocalContext.current.applicationContext as ProPaintersApplication
    val contentResolver = LocalContext.current.contentResolver
    val viewModel: BackupRestoreViewModel = viewModel(
        factory = BackupRestoreViewModel.provideFactory(application.container.backupRestoreService)
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.restoreComplete.collect { onRestoreComplete() }
    }

    StartupChoiceScreen(
        uiState = uiState,
        onStartNew = onStartNew,
        onRestoreSelected = { uri -> viewModel.prepareRestore(contentResolver, uri) },
        onConfirmRestore = { viewModel.confirmRestore(contentResolver) },
        onCancelRestore = viewModel::cancelRestore,
        onMessageShown = viewModel::clearUserMessage,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BackupRestoreScreen(
    uiState: BackupRestoreUiState,
    onBack: () -> Unit,
    onBackupSelected: (Uri) -> Unit,
    onRestoreSelected: (Uri) -> Unit,
    onConfirmRestore: () -> Unit,
    onCancelRestore: () -> Unit,
    onMessageShown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let(onBackupSelected) }
    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let(onRestoreSelected) }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onMessageShown()
        }
    }

    RestoreConfirmationDialog(
        restoreInfo = uiState.pendingRestoreInfo,
        isRunning = uiState.isRunning,
        onConfirmRestore = onConfirmRestore,
        onCancelRestore = onCancelRestore
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CharcoalBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text("Backup & Restore", color = OffWhite, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = IndustrialGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CharcoalBackground)
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(AppDimensions.screenPadding),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            BackupRestoreCard(
                uiState = uiState,
                onBackupClick = { backupLauncher.launch(defaultBackupFileName()) },
                onRestoreClick = { restoreLauncher.launch(arrayOf("application/json", "text/*", "*/*")) }
            )
        }
    }
}

@Composable
private fun StartupChoiceScreen(
    uiState: BackupRestoreUiState,
    onStartNew: () -> Unit,
    onRestoreSelected: (Uri) -> Unit,
    onConfirmRestore: () -> Unit,
    onCancelRestore: () -> Unit,
    onMessageShown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let(onRestoreSelected) }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onMessageShown()
        }
    }

    RestoreConfirmationDialog(
        restoreInfo = uiState.pendingRestoreInfo,
        isRunning = uiState.isRunning,
        onConfirmRestore = onConfirmRestore,
        onCancelRestore = onCancelRestore
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CharcoalBackground,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(AppDimensions.screenPadding),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "PRO PAINTERS",
                style = MaterialTheme.typography.headlineLarge,
                color = IndustrialGold,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Start fresh or restore your existing backup.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            PrimaryButton(
                text = "Start New",
                onClick = onStartNew,
                enabled = !uiState.isRunning
            )
            Spacer(modifier = Modifier.height(16.dp))
            SecondaryButton(
                text = "Restore From Backup",
                onClick = { restoreLauncher.launch(arrayOf("application/json", "text/*", "*/*")) },
                enabled = !uiState.isRunning,
                icon = { Icon(Icons.Default.Restore, null) }
            )
        }
    }
}

@Composable
private fun BackupRestoreCard(
    uiState: BackupRestoreUiState,
    onBackupClick: () -> Unit,
    onRestoreClick: () -> Unit
) {
    IndustrialCard {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "Local Data",
                style = MaterialTheme.typography.titleMedium,
                color = IndustrialGold,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Save a JSON backup of your app data, or restore from a previous backup file.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )
            PrimaryButton(
                text = "Backup Data",
                onClick = onBackupClick,
                enabled = !uiState.isRunning,
                icon = {
                    if (uiState.isRunning) {
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
            SecondaryButton(
                text = "Restore Data",
                onClick = onRestoreClick,
                enabled = !uiState.isRunning,
                icon = { Icon(Icons.Default.Restore, null) }
            )
        }
    }
}

@Composable
private fun RestoreConfirmationDialog(
    restoreInfo: BackupFileInfo?,
    isRunning: Boolean,
    onConfirmRestore: () -> Unit,
    onCancelRestore: () -> Unit
) {
    restoreInfo ?: return

    AlertDialog(
        onDismissRequest = onCancelRestore,
        containerColor = CharcoalSecondary,
        titleContentColor = IndustrialGold,
        textContentColor = OffWhite,
        title = {
            Text("Restore backup?", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("This will replace all current app data with the selected backup.")
                Text(
                    text = "Created: ${restoreInfo.createdAt}",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Records: ${restoreInfo.totalRows}",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmRestore,
                enabled = !isRunning,
                colors = ButtonDefaults.buttonColors(
                    containerColor = IndustrialGold,
                    contentColor = CharcoalBackground
                )
            ) {
                Text("RESTORE", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(
                onClick = onCancelRestore,
                enabled = !isRunning,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CharcoalMuted,
                    contentColor = OffWhite
                ),
                border = BorderStroke(1.dp, TextMuted)
            ) {
                Text("CANCEL", fontWeight = FontWeight.Bold)
            }
        }
    )
}

private fun defaultBackupFileName(): String {
    val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.US).format(Date())
    return "ProPaintersBackup_$timestamp.json"
}
