package com.example.propaintersplastererspayment.feature.backup.vm

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.propaintersplastererspayment.data.backup.BackupFileInfo
import com.example.propaintersplastererspayment.data.backup.BackupRestoreService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BackupRestoreUiState(
    val isRunning: Boolean = false,
    val pendingRestoreInfo: BackupFileInfo? = null,
    val userMessage: String? = null
)

class BackupRestoreViewModel(
    private val backupRestoreService: BackupRestoreService
) : ViewModel() {
    private val _uiState = MutableStateFlow(BackupRestoreUiState())
    val uiState: StateFlow<BackupRestoreUiState> = _uiState.asStateFlow()

    private val _restoreComplete = MutableSharedFlow<Unit>()
    val restoreComplete: SharedFlow<Unit> = _restoreComplete.asSharedFlow()

    private var pendingRestoreUri: Uri? = null

    fun exportBackup(contentResolver: ContentResolver, uri: Uri) {
        if (_uiState.value.isRunning) return

        _uiState.update { it.copy(isRunning = true) }
        viewModelScope.launch {
            try {
                val summary = contentResolver.openOutputStream(uri)?.use { outputStream ->
                    backupRestoreService.exportBackup(outputStream)
                } ?: throw IllegalStateException("Could not open backup file.")

                _uiState.update {
                    it.copy(userMessage = "Backup saved successfully (${summary.totalRows} records).")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(userMessage = "Backup failed: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isRunning = false) }
            }
        }
    }

    fun prepareRestore(contentResolver: ContentResolver, uri: Uri) {
        if (_uiState.value.isRunning) return

        _uiState.update { it.copy(isRunning = true) }
        viewModelScope.launch {
            try {
                val info = contentResolver.openInputStream(uri)?.use { inputStream ->
                    backupRestoreService.inspectBackup(inputStream)
                } ?: throw IllegalStateException("Could not open backup file.")

                pendingRestoreUri = uri
                _uiState.update { it.copy(pendingRestoreInfo = info) }
            } catch (e: Exception) {
                pendingRestoreUri = null
                _uiState.update {
                    it.copy(
                        pendingRestoreInfo = null,
                        userMessage = "Could not read backup: ${e.message}"
                    )
                }
            } finally {
                _uiState.update { it.copy(isRunning = false) }
            }
        }
    }

    fun cancelRestore() {
        pendingRestoreUri = null
        _uiState.update { it.copy(pendingRestoreInfo = null) }
    }

    fun confirmRestore(contentResolver: ContentResolver) {
        val uri = pendingRestoreUri ?: return
        if (_uiState.value.isRunning) return

        _uiState.update { it.copy(isRunning = true) }
        viewModelScope.launch {
            try {
                val summary = contentResolver.openInputStream(uri)?.use { inputStream ->
                    backupRestoreService.restoreBackup(inputStream)
                } ?: throw IllegalStateException("Could not open backup file.")

                pendingRestoreUri = null
                _uiState.update {
                    it.copy(
                        pendingRestoreInfo = null,
                        userMessage = "Restore complete (${summary.totalRows} records)."
                    )
                }
                _restoreComplete.emit(Unit)
            } catch (e: Exception) {
                _uiState.update { it.copy(userMessage = "Restore failed: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isRunning = false) }
            }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    companion object {
        fun provideFactory(
            backupRestoreService: BackupRestoreService
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return BackupRestoreViewModel(backupRestoreService) as T
            }
        }
    }
}
