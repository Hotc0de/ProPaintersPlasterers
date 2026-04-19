package com.example.propaintersplastererspayment.data

import com.example.propaintersplastererspayment.domain.repository.AccessRepository
import com.example.propaintersplastererspayment.domain.repository.ClientRepository
import com.example.propaintersplastererspayment.domain.repository.InvoiceRepository
import com.example.propaintersplastererspayment.domain.repository.JobRepository
import com.example.propaintersplastererspayment.domain.repository.MaterialRepository
import com.example.propaintersplastererspayment.domain.repository.SettingsRepository
import com.example.propaintersplastererspayment.domain.repository.PaintRepository
import com.example.propaintersplastererspayment.domain.repository.WorkEntryRepository

interface AppContainer {
    val jobRepository: JobRepository
    val workEntryRepository: WorkEntryRepository
    val materialRepository: MaterialRepository
    val clientRepository: ClientRepository
    val invoiceRepository: InvoiceRepository
    val settingsRepository: SettingsRepository
    val accessRepository: AccessRepository
    val paintRepository: PaintRepository
}

