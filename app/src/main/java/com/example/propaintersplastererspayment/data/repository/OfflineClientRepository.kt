package com.example.propaintersplastererspayment.data.repository

import com.example.propaintersplastererspayment.data.local.dao.ClientDao
import com.example.propaintersplastererspayment.data.local.entity.ClientEntity
import com.example.propaintersplastererspayment.domain.repository.ClientRepository
import kotlinx.coroutines.flow.Flow

/**
 * Concrete implementation of [ClientRepository] backed by the local Room database.
 */
class OfflineClientRepository(
    private val clientDao: ClientDao
) : ClientRepository {

    override fun observeClients(): Flow<List<ClientEntity>> =
        clientDao.observeClients()

    override fun observeSuggestions(query: String): Flow<List<ClientEntity>> =
        clientDao.observeClientSuggestions(query)

    /** Insert a new client (clientId == 0) or update an existing one. */
    override suspend fun saveClient(client: ClientEntity): Long {
        return if (client.clientId == 0L) {
            clientDao.insertClient(client)
        } else {
            clientDao.updateClient(client)
            client.clientId
        }
    }

    override suspend fun deleteClient(client: ClientEntity) =
        clientDao.deleteClient(client)

    override suspend fun getClient(clientId: Long): ClientEntity? =
        clientDao.getClientById(clientId)
}
