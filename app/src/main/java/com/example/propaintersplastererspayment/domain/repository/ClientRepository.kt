package com.example.propaintersplastererspayment.domain.repository

import com.example.propaintersplastererspayment.data.local.entity.ClientEntity
import kotlinx.coroutines.flow.Flow

interface ClientRepository {
    // Stream of all clients, sorted alphabetically
    fun observeClients(): Flow<List<ClientEntity>>

    // Stream of clients whose name contains the search query (used for auto-suggestions)
    fun observeSuggestions(query: String): Flow<List<ClientEntity>>

    // Insert or update a client; returns the saved clientId
    suspend fun saveClient(client: ClientEntity): Long

    // Delete a client record
    suspend fun deleteClient(client: ClientEntity)

    // Look up a single client by ID
    suspend fun getClient(clientId: Long): ClientEntity?
}
