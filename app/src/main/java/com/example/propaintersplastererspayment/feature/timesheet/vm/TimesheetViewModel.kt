package com.example.propaintersplastererspayment.feature.timesheet.vm

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.propaintersplastererspayment.core.pdf.MaterialPdfRow
import com.example.propaintersplastererspayment.core.pdf.PdfBusinessDetails
import com.example.propaintersplastererspayment.core.pdf.TimesheetPdfData
import com.example.propaintersplastererspayment.core.pdf.WorkEntryPdfRow
import com.example.propaintersplastererspayment.core.util.DateFormatUtils
import com.example.propaintersplastererspayment.core.util.WorkEntryTimeUtils
import com.example.propaintersplastererspayment.data.local.entity.AppSettingsEntity
import com.example.propaintersplastererspayment.data.local.entity.JobEntity
import com.example.propaintersplastererspayment.data.local.entity.WorkEntryEntity
import com.example.propaintersplastererspayment.data.local.entity.MaterialItemEntity
import com.example.propaintersplastererspayment.domain.repository.JobRepository
import com.example.propaintersplastererspayment.domain.repository.MaterialRepository
import com.example.propaintersplastererspayment.domain.repository.SettingsRepository
import com.example.propaintersplastererspayment.domain.repository.WorkEntryRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
private fun defaultWorkDate(): String = DateFormatUtils.todayDisplayDate()

data class WorkEntryFormState(
    val entryId: Long? = null,
    val workDate: String = defaultWorkDate(),
    val workerName: String = "",
    val startTime: TextFieldValue = TextFieldValue(""),
    val finishTime: TextFieldValue = TextFieldValue(""),
    val errorMessage: String? = null
) {
    val calculatedHours: Double?
        get() = WorkEntryTimeUtils.calculateHoursWorked(startTime = startTime.text, finishTime = finishTime.text)
}

data class TimesheetUiState(
    val job: JobEntity? = null,
    val entries: List<WorkEntryEntity> = emptyList(),
    val materials: List<MaterialItemEntity> = emptyList(),
    val totalHours: Double = 0.0,
    val totalMaterialCost: Double = 0.0,
    val isLuxuryPreviewMode: Boolean = false,
    val isFormVisible: Boolean = false,
    val formState: WorkEntryFormState = WorkEntryFormState(),
    val userMessage: String? = null,
    val isLoading: Boolean = true
)

class TimesheetViewModel(
    private val jobId: Long,
    private val jobRepository: JobRepository,
    private val workEntryRepository: WorkEntryRepository,
    private val materialRepository: MaterialRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val isFormVisible = MutableStateFlow(false)
    private val isLuxuryPreviewMode = MutableStateFlow(false)
    private val formState = MutableStateFlow(WorkEntryFormState())
    private val userMessage = MutableStateFlow<String?>(null)
    private val pdfExportRequests = MutableSharedFlow<TimesheetPdfData>()

    val pdfExportEvents: SharedFlow<TimesheetPdfData> = pdfExportRequests.asSharedFlow()

    private val timesheetData = combine(
        jobRepository.observeJob(jobId),
        workEntryRepository.observeEntriesForJob(jobId),
        workEntryRepository.observeTotalHoursForJob(jobId),
        materialRepository.observeMaterialsForJob(jobId)
    ) { job, entries, totalHours, materials ->
        val totalMaterialCost = materials.sumOf { it.price }
        DataSnapshot(job, entries, totalHours, materials, totalMaterialCost)
    }

    private data class DataSnapshot(
        val job: JobEntity?,
        val entries: List<WorkEntryEntity>,
        val totalHours: Double,
        val materials: List<MaterialItemEntity>,
        val totalMaterialCost: Double
    )

    val uiState: StateFlow<TimesheetUiState> = combine(
        timesheetData,
        isFormVisible,
        isLuxuryPreviewMode,
        formState,
        userMessage
    ) { snapshot, formVisible, luxuryPreview, form, message ->
        TimesheetUiState(
            job = snapshot.job,
            entries = snapshot.entries,
            materials = snapshot.materials,
            totalHours = snapshot.totalHours,
            totalMaterialCost = snapshot.totalMaterialCost,
            isLuxuryPreviewMode = luxuryPreview,
            isFormVisible = formVisible,
            formState = form,
            userMessage = message,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TimesheetUiState()
    )

    fun toggleLuxuryPreview() {
        isLuxuryPreviewMode.update { !it }
    }

    fun openAddEntry() {
        formState.value = WorkEntryFormState()
        isFormVisible.value = true
    }

    fun openEditEntry(entry: WorkEntryEntity) {
        formState.value = WorkEntryFormState(
            entryId = entry.entryId,
            workDate = DateFormatUtils.formatDisplayDate(entry.workDate),
            workerName = entry.workerName,
            startTime = TextFieldValue(entry.startTime, selection = androidx.compose.ui.text.TextRange(entry.startTime.length)),
            finishTime = TextFieldValue(entry.finishTime, selection = androidx.compose.ui.text.TextRange(entry.finishTime.length)),
            errorMessage = null
        )
        isFormVisible.value = true
    }

    fun dismissForm() {
        isFormVisible.value = false
        formState.value = WorkEntryFormState()
    }

    fun onWorkDateChange(value: String) {
        formState.update { current -> current.copy(workDate = value, errorMessage = null) }
    }

    fun onWorkerNameChange(value: String) {
        formState.update { current -> current.copy(workerName = value, errorMessage = null) }
    }

    fun onStartTimeChange(value: TextFieldValue) {
        formState.update { current -> current.copy(startTime = value, errorMessage = null) }
    }

    fun onFinishTimeChange(value: TextFieldValue) {
        formState.update { current -> current.copy(finishTime = value, errorMessage = null) }
    }

    fun clearUserMessage() {
        userMessage.value = null
    }

    fun exportTimesheetPdf() {
        viewModelScope.launch {
            val snapshot = uiState.value
            val job = snapshot.job
            if (job == null) {
                userMessage.value = "Cannot export PDF. Job not found."
                return@launch
            }

            val settings = settingsRepository.observeSettings().first() ?: AppSettingsEntity()
            val materials = materialRepository.observeMaterialsForJob(jobId).first()

            val data = TimesheetPdfData(
                fileName = "timesheet-${job.jobId}-${System.currentTimeMillis()}.pdf",
                exportedAt = DateFormatUtils.todayDisplayDate(),
                business = settings.toBusinessDetails(),
                jobName = job.jobName,
                jobAddress = job.propertyAddress,
                workEntries = snapshot.entries.map { entry ->
                    WorkEntryPdfRow(
                        workDate = DateFormatUtils.formatDisplayDate(entry.workDate),
                        workerName = entry.workerName,
                        startTime = entry.startTime,
                        finishTime = entry.finishTime,
                        hoursWorked = entry.hoursWorked
                    )
                },
                totalHours = snapshot.totalHours,
                materials = materials.map { item ->
                    MaterialPdfRow(
                        materialName = item.materialName,
                        price = item.price
                    )
                },
                totalMaterialCost = materials.sumOf { it.price }
            )
            pdfExportRequests.emit(data)
        }
    }

    fun onPdfExportFinished(success: Boolean) {
        userMessage.value = if (success) {
            "Timesheet PDF exported."
        } else {
            "Failed to export timesheet PDF."
        }
    }

    fun saveEntry() {
        val currentForm = formState.value
        if (currentForm.workerName.isBlank()) {
            formState.update { it.copy(errorMessage = "Worker name is required.") }
            return
        }
        val startTime = currentForm.startTime.text
        val finishTime = currentForm.finishTime.text
        val hoursWorked = WorkEntryTimeUtils.calculateHoursWorked(startTime, finishTime)
        
        if (hoursWorked == null) {
            formState.update { it.copy(errorMessage = "Invalid time format. Use HH:mm.") }
            return
        }

        val storedWorkDate = DateFormatUtils.toStoredDate(currentForm.workDate)
        if (storedWorkDate == null) {
            formState.update { it.copy(errorMessage = "Invalid date format.") }
            return
        }

        viewModelScope.launch {
            workEntryRepository.saveEntry(
                WorkEntryEntity(
                    entryId = currentForm.entryId ?: 0,
                    jobOwnerId = jobId,
                    workDate = storedWorkDate,
                    workerName = currentForm.workerName.trim(),
                    startTime = startTime,
                    finishTime = finishTime,
                    hoursWorked = hoursWorked
                )
            )

            userMessage.value = if (currentForm.entryId == null) {
                "Timesheet entry added."
            } else {
                "Timesheet entry updated."
            }
            dismissForm()
        }
    }

    fun deleteEntry(entryId: Long) {
        viewModelScope.launch {
            val entry = workEntryRepository.getEntry(entryId) ?: return@launch
            workEntryRepository.deleteEntry(entry)
            userMessage.value = "Timesheet entry deleted."

            if (formState.value.entryId == entryId) {
                dismissForm()
            }
        }
    }

    companion object {
        fun provideFactory(
            jobId: Long,
            jobRepository: JobRepository,
            workEntryRepository: WorkEntryRepository,
            materialRepository: MaterialRepository,
            settingsRepository: SettingsRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TimesheetViewModel(
                    jobId = jobId,
                    jobRepository = jobRepository,
                    workEntryRepository = workEntryRepository,
                    materialRepository = materialRepository,
                    settingsRepository = settingsRepository
                ) as T
            }
        }
    }
}

private fun AppSettingsEntity.toBusinessDetails(): PdfBusinessDetails = PdfBusinessDetails(
    businessName = businessName,
    address = address,
    phoneNumber = phoneNumber,
    email = email,
    gstNumber = gstNumber,
    bankAccountNumber = bankAccountNumber,
    bankName = bankName
)



