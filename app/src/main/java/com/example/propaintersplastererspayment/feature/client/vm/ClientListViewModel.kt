package com.example.propaintersplastererspayment.feature.client.vm

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.propaintersplastererspayment.data.local.entity.ClientEntity
import com.example.propaintersplastererspayment.domain.repository.ClientRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ClientListUiState(
    val clients: List<ClientEntity> = emptyList(),
    val searchQuery: TextFieldValue = TextFieldValue(""),
    val isLoading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
class ClientListViewModel(
    private val clientRepository: ClientRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow(TextFieldValue(""))

    private val filteredClients: StateFlow<List<ClientEntity>> = searchQuery
        .map { it.text }
        .flatMapLatest { query ->
            if (query.isBlank()) {
                clientRepository.observeClients()
            } else {
                clientRepository.observeSuggestions(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val uiState: StateFlow<ClientListUiState> = combine(
        filteredClients,
        searchQuery
    ) { clients, query ->
        ClientListUiState(
            clients = clients,
            searchQuery = query,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ClientListUiState()
    )

    fun onSearchQueryChange(query: TextFieldValue) {
        searchQuery.update { query }
    }

    fun deleteClient(client: ClientEntity) {
        viewModelScope.launch {
            clientRepository.deleteClient(client)
        }
    }

    companion object {
        fun provideFactory(clientRepository: ClientRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ClientListViewModel(clientRepository) as T
            }
    }
}

