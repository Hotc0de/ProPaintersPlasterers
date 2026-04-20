package com.example.propaintersplastererspayment.feature.paint.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.propaintersplastererspayment.data.local.entity.PaintBrandEntity
import com.example.propaintersplastererspayment.data.local.entity.PaintItemEntity
import com.example.propaintersplastererspayment.domain.repository.PaintRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PaintViewModel(val paintRepository: PaintRepository) : ViewModel() {

    val brands: StateFlow<List<PaintBrandEntity>> = paintRepository.getAllBrandsStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addBrand(name: String) {
        viewModelScope.launch {
            paintRepository.insertBrand(PaintBrandEntity(brandName = name))
        }
    }

    fun getPaintsForBrand(brandId: Long): StateFlow<List<PaintItemEntity>> {
        return paintRepository.getPaintsForBrandStream(brandId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun getPaintById(paintId: Long): StateFlow<PaintItemEntity?> {
        return paintRepository.getPaintByIdStream(paintId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }

    fun addPaint(brandId: Long, name: String, code: String, hex: String, finishType: String = "", paintScope: String = "Interior", notes: String = "") {
        viewModelScope.launch {
            paintRepository.insertPaint(
                PaintItemEntity(
                    brandId = brandId,
                    paintName = name,
                    paintCode = code,
                    hexCode = hex,
                    finishType = finishType,
                    paintScope = paintScope,
                    notes = notes
                )
            )
        }
    }

    fun updatePaint(paintId: Long, brandId: Long, name: String, code: String, hex: String, finishType: String = "", paintScope: String = "Interior", notes: String = "") {
        viewModelScope.launch {
            paintRepository.updatePaint(
                PaintItemEntity(
                    paintId = paintId,
                    brandId = brandId,
                    paintName = name,
                    paintCode = code,
                    hexCode = hex,
                    finishType = finishType,
                    paintScope = paintScope,
                    notes = notes
                )
            )
        }
    }

    fun deletePaint(paint: PaintItemEntity) {
        viewModelScope.launch {
            paintRepository.deletePaint(paint)
        }
    }

    companion object {
        fun provideFactory(paintRepository: PaintRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PaintViewModel(paintRepository) as T
                }
            }
    }
}
