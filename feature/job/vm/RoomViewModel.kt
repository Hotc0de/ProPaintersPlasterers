package com.example.propaintersplastererspayment.feature.job.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.propaintersplastererspayment.data.local.entity.RoomEntity
import com.example.propaintersplastererspayment.data.local.model.RoomWithSurfaces
import com.example.propaintersplastererspayment.domain.repository.RoomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RoomUiState(
    val rooms: List<RoomWithSurfaces> = emptyList(),
    val isLoading: Boolean = true,
    val selectedRoom: RoomEntity? = null,
    val isShowingRoomForm: Boolean = false,
    val errorMessage: String? = null
)

class RoomViewModel(
    private val jobId: Long,
    private val roomRepository: RoomRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoomUiState())
    val uiState: StateFlow<RoomUiState> = _uiState.asStateFlow()

    init {
        observeRooms()
    }

    private fun observeRooms() {
        viewModelScope.launch {
            roomRepository.observeRoomsForJob(jobId).collect { rooms ->
                _uiState.update { it.copy(rooms = rooms, isLoading = false) }
            }
        }
    }

    fun onAddRoomClick() {
        _uiState.update { 
            it.copy(
                selectedRoom = RoomEntity(jobId = jobId),
                isShowingRoomForm = true 
            ) 
        }
    }

    fun onEditRoomClick(room: RoomEntity) {
        _uiState.update { 
            it.copy(
                selectedRoom = room,
                isShowingRoomForm = true 
            ) 
        }
    }

    fun onDismissRoomForm() {
        _uiState.update { 
            it.copy(
                selectedRoom = null,
                isShowingRoomForm = false 
            ) 
        }
    }

    fun saveRoom(room: RoomEntity) {
        viewModelScope.launch {
            try {
                roomRepository.saveRoom(room)
                onDismissRoomForm()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to save room: ${e.message}") }
            }
        }
    }

    fun deleteRoom(room: RoomEntity) {
        viewModelScope.launch {
            try {
                roomRepository.deleteRoom(room)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to delete room: ${e.message}") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    companion object {
        fun provideFactory(
            jobId: Long,
            roomRepository: RoomRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return RoomViewModel(jobId, roomRepository) as T
            }
        }
    }
}
