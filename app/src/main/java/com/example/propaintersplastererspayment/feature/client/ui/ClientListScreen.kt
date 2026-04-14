package com.example.propaintersplastererspayment.feature.client.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.propaintersplastererspayment.ProPaintersApplication
import com.example.propaintersplastererspayment.data.local.entity.ClientEntity
import com.example.propaintersplastererspayment.feature.client.vm.ClientListViewModel
import com.example.propaintersplastererspayment.ui.components.IndustrialCard
import com.example.propaintersplastererspayment.ui.components.IndustrialFAB
import com.example.propaintersplastererspayment.ui.components.IndustrialTextField
import com.example.propaintersplastererspayment.ui.theme.*

@Composable
fun ClientListRoute(
    onAddClient: () -> Unit,
    onEditClient: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val application = LocalContext.current.applicationContext as ProPaintersApplication
    val viewModel: ClientListViewModel = viewModel(
        factory = ClientListViewModel.provideFactory(application.container.clientRepository)
    )
    val uiState by viewModel.uiState.collectAsState()

    ClientListScreen(
        clients = uiState.clients,
        searchQuery = uiState.searchQuery,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onAddClient = onAddClient,
        onEditClient = onEditClient,
        onDeleteClient = viewModel::deleteClient,
        onBack = onBack,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientListScreen(
    clients: List<ClientEntity>,
    searchQuery: TextFieldValue,
    onSearchQueryChange: (TextFieldValue) -> Unit,
    onAddClient: () -> Unit,
    onEditClient: (Long) -> Unit,
    onDeleteClient: (ClientEntity) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CharcoalBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Clients",
                        style = MaterialTheme.typography.headlineSmall,
                        color = IndustrialGold,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = IndustrialGold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CharcoalBackground,
                    titleContentColor = OffWhite
                )
            )
        },
        floatingActionButton = {
            IndustrialFAB(onClick = onAddClient)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = AppDimensions.screenPadding)
        ) {
            IndustrialTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                label = "Search",
                placeholder = "Search by name or email...",
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = IndustrialGold
                    )
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
            )

            if (clients.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (searchQuery.text.isBlank()) "No clients yet" else "No matching results",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextMuted
                        )
                        Text(
                            text = if (searchQuery.text.isBlank()) "Tap + to add your first client" else "Try a different search term",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSubdued,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp)
                ) {
                    items(clients, key = { it.clientId }) { client ->
                        ClientIndustrialCard(
                            client = client,
                            onEdit = { onEditClient(client.clientId) },
                            onDelete = { onDeleteClient(client) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClientIndustrialCard(
    client: ClientEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    IndustrialCard(onClick = onEdit) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = AppShapes.medium,
                        color = IndustrialGold.copy(alpha = 0.1f)
                    ) {
                        Icon(
                            imageVector = if (client.clientType == "BUSINESS") Icons.Default.Business else Icons.Default.Person,
                            contentDescription = null,
                            tint = IndustrialGold,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Column {
                        Text(
                            text = client.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OffWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (client.clientType == "BUSINESS") "Business Client" else "Private Client",
                            style = MaterialTheme.typography.labelSmall,
                            color = IndustrialGold
                        )
                    }
                }
                
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = ErrorRed.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            HorizontalDivider(color = BorderColor)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (client.address.isNotBlank()) {
                    ClientInfoRow(icon = Icons.Default.LocationOn, text = client.address)
                }
                if (client.phoneNumber.isNotBlank()) {
                    ClientInfoRow(icon = Icons.Default.Phone, text = client.phoneNumber)
                }
                if (client.email.isNotBlank()) {
                    ClientInfoRow(icon = Icons.Default.Email, text = client.email)
                }
            }
        }
    }
}

@Composable
private fun ClientInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = OffWhite,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
