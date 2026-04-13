package com.example.propaintersplastererspayment.feature.materials.vm

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.propaintersplastererspayment.core.util.MaterialValidationUtils
import com.example.propaintersplastererspayment.data.local.entity.JobEntity
import com.example.propaintersplastererspayment.data.local.entity.MaterialItemEntity
import com.example.propaintersplastererspayment.domain.repository.JobRepository
import com.example.propaintersplastererspayment.domain.repository.MaterialRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MaterialFormState(
    val materialId: Long? = null,
    val materialName: TextFieldValue = TextFieldValue(""),
    val priceText: TextFieldValue = TextFieldValue(""),
    val errorMessage: String? = null
) {
    val parsedPrice: Double?
        get() = MaterialValidationUtils.parsePrice(priceText.text)
}

data class MaterialsUiState(
    val job: JobEntity? = null,
    val materials: List<MaterialItemEntity> = emptyList(),
    val totalMaterialCost: Double = 0.0,
    val isFormVisible: Boolean = false,
    val formState: MaterialFormState = MaterialFormState(),
    val userMessage: String? = null,
    val isLoading: Boolean = true
)

class MaterialsViewModel(
    private val jobId: Long,
    private val jobRepository: JobRepository,
    private val materialRepository: MaterialRepository
) : ViewModel() {

    private val isFormVisible = MutableStateFlow(false)
    private val formState = MutableStateFlow(MaterialFormState())
    private val userMessage = MutableStateFlow<String?>(null)

    private val materialsData = combine(
        jobRepository.observeJob(jobId),
        materialRepository.observeMaterialsForJob(jobId),
        materialRepository.observeTotalMaterialCostForJob(jobId)
    ) { job, materials, totalMaterialCost ->
        Triple(job, materials, totalMaterialCost)
    }

    val uiState: StateFlow<MaterialsUiState> = combine(
        materialsData,
        isFormVisible,
        formState,
        userMessage
    ) { materialsData, formVisible, form, message ->
        val (job, materials, totalMaterialCost) = materialsData
        MaterialsUiState(
            job = job,
            materials = materials,
            totalMaterialCost = totalMaterialCost,
            isFormVisible = formVisible,
            formState = form,
            userMessage = message,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MaterialsUiState()
    )

    fun openAddMaterial() {
        formState.value = MaterialFormState()
        isFormVisible.value = true
    }

    fun openEditMaterial(item: MaterialItemEntity) {
        val priceStr = item.price.toString()
        formState.value = MaterialFormState(
            materialId = item.materialId,
            materialName = TextFieldValue(item.materialName, selection = androidx.compose.ui.text.TextRange(item.materialName.length)),
            priceText = TextFieldValue(priceStr, selection = androidx.compose.ui.text.TextRange(priceStr.length)),
            errorMessage = null
        )
        isFormVisible.value = true
    }

    fun dismissForm() {
        isFormVisible.value = false
        formState.value = MaterialFormState()
    }

    fun onMaterialNameChange(value: TextFieldValue) {
        formState.update { current -> current.copy(materialName = value, errorMessage = null) }
    }

    fun onPriceChange(value: TextFieldValue) {
        formState.update { current -> current.copy(priceText = value, errorMessage = null) }
    }

    fun clearUserMessage() {
        userMessage.value = null
    }

    fun saveMaterial() {
        val currentForm = formState.value
        val materialName = currentForm.materialName.text.trim()
        val price = currentForm.parsedPrice

        if (materialName.isBlank()) {
            formState.update { it.copy(errorMessage = "Material name is required.") }
            return
        }
        if (price == null || price <= 0) {
            formState.update { it.copy(errorMessage = "Invalid price.") }
            return
        }

        viewModelScope.launch {
            materialRepository.saveMaterial(
                MaterialItemEntity(
                    materialId = currentForm.materialId ?: 0,
                    jobOwnerId = jobId,
                    materialName = materialName,
                    price = price
                )
            )

            userMessage.value = if (currentForm.materialId == null) {
                "Material added."
            } else {
                "Material updated."
            }
            dismissForm()
        }
    }

    fun deleteMaterial(materialId: Long) {
        viewModelScope.launch {
            val material = materialRepository.getMaterial(materialId) ?: return@launch
            materialRepository.deleteMaterial(material)
            userMessage.value = "Material deleted."

            if (formState.value.materialId == materialId) {
                dismissForm()
            }
        }
    }

    companion object {
        fun provideFactory(
            jobId: Long,
            jobRepository: JobRepository,
            materialRepository: MaterialRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MaterialsViewModel(
                    jobId = jobId,
                    jobRepository = jobRepository,
                    materialRepository = materialRepository
                ) as T
            }
        }
    }
}

