package com.example.propaintersplastererspayment.feature.job.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.propaintersplastererspayment.data.local.dao.JobPaintDisplay
import com.example.propaintersplastererspayment.data.local.entity.SurfaceEntity
import com.example.propaintersplastererspayment.data.local.model.SurfaceWithJobPaint
import com.example.propaintersplastererspayment.domain.repository.PaintRepository
import com.example.propaintersplastererspayment.domain.repository.RoomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SurfaceUiState(
    val surfaces: List<SurfaceWithJobPaint> = emptyList(),
    val availablePaints: List<JobPaintDisplay> = emptyList(),
    val isLoading: Boolean = true,
    val selectedSurface: SurfaceEntity? = null,
    val isShowingSurfaceForm: Boolean = false,
    val errorMessage: String? = null
)

class SurfaceViewModel(
    private val jobId: Long,
    private val roomId: Long,
    private val roomRepository: RoomRepository,
    private val paintRepository: PaintRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SurfaceUiState())
    val uiState: StateFlow<SurfaceUiState> = _uiState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                roomRepository.observeSurfacesForRoom(roomId),
                paintRepository.getPaintsForJobStream(jobId)
            ) { surfaces, paints ->
                _uiState.update { it.copy(
                    surfaces = surfaces,
                    availablePaints = paints,
                    isLoading = false
                ) }
            }.collect {}
        }
    }

    fun onAddSurfaceClick() {
        _uiState.update { 
            it.copy(
                selectedSurface = SurfaceEntity(roomId = roomId),
                isShowingSurfaceForm = true 
            ) 
        }
    }

    fun onEditSurfaceClick(surface: SurfaceEntity) {
        _uiState.update { 
            it.copy(
                selectedSurface = surface,
                isShowingSurfaceForm = true 
            ) 
        }
    }

    fun onDismissSurfaceForm() {
        _uiState.update { 
            it.copy(
                selectedSurface = null,
                isShowingSurfaceForm = false 
            ) 
        }
    }

    fun saveSurface(surface: SurfaceEntity) {
        viewModelScope.launch {
            try {
                roomRepository.saveSurface(surface)
                onDismissSurfaceForm()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to save surface: ${e.message}") }
            }
        }
    }

    fun deleteSurface(surface: SurfaceEntity) {
        viewModelScope.launch {
            try {
                roomRepository.deleteSurface(surface)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to delete surface: ${e.message}") }
            }
        }
    }

    fun duplicateSurface(surfaceId: Long) {
        viewModelScope.launch {
            try {
                roomRepository.duplicateSurface(surfaceId)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to duplicate surface: ${e.message}") }
            }
        }
    }

    fun updateSurfacePaint(surfaceId: Long, jobPaintId: Long?) {
        viewModelScope.launch {
            try {
                roomRepository.updateSurfacePaint(surfaceId, jobPaintId)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to update paint: ${e.message}") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    companion object {
        fun provideFactory(
            jobId: Long,
            roomId: Long,
            roomRepository: RoomRepository,
            paintRepository: PaintRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SurfaceViewModel(jobId, roomId, roomRepository, paintRepository) as T
            }
        }
    }
}
