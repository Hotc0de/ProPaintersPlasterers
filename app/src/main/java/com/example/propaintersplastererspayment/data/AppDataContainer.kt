package com.example.propaintersplastererspayment.data

import android.content.Context
import androidx.room.Room
import com.example.propaintersplastererspayment.data.local.AppDatabase
import com.example.propaintersplastererspayment.data.repository.OfflineAccessRepository
import com.example.propaintersplastererspayment.data.repository.OfflineClientRepository
import com.example.propaintersplastererspayment.data.repository.OfflineInvoiceRepository
import com.example.propaintersplastererspayment.data.repository.OfflineJobRepository
import com.example.propaintersplastererspayment.data.repository.OfflineMaterialRepository
import com.example.propaintersplastererspayment.data.repository.OfflinePaintRepository
import com.example.propaintersplastererspayment.data.repository.OfflineRoomRepository
import com.example.propaintersplastererspayment.data.repository.OfflineSettingsRepository
import com.example.propaintersplastererspayment.data.repository.OfflineWorkEntryRepository
import com.example.propaintersplastererspayment.domain.repository.AccessRepository
import com.example.propaintersplastererspayment.domain.repository.ClientRepository
import com.example.propaintersplastererspayment.domain.repository.InvoiceRepository
import com.example.propaintersplastererspayment.domain.repository.JobRepository
import com.example.propaintersplastererspayment.domain.repository.MaterialRepository
import com.example.propaintersplastererspayment.domain.repository.PaintRepository
import com.example.propaintersplastererspayment.domain.repository.RoomRepository
import com.example.propaintersplastererspayment.domain.repository.SettingsRepository
import com.example.propaintersplastererspayment.domain.repository.WorkEntryRepository

class AppDataContainer(
    private val context: Context
) : AppContainer {
    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "propainters_db"
        )
            .addMigrations(
                AppDatabase.MIGRATION_11_12, 
                AppDatabase.MIGRATION_12_13, 
                AppDatabase.MIGRATION_13_14,
                AppDatabase.MIGRATION_14_15,
                AppDatabase.MIGRATION_15_16,
                AppDatabase.MIGRATION_16_17,
                AppDatabase.MIGRATION_17_18,
                AppDatabase.MIGRATION_18_19
            )
            .fallbackToDestructiveMigration()
            .build()
    }

    override val jobRepository: JobRepository by lazy {
        OfflineJobRepository(database.jobDao())
    }

    override val workEntryRepository: WorkEntryRepository by lazy {
        OfflineWorkEntryRepository(database.workEntryDao())
    }

    override val materialRepository: MaterialRepository by lazy {
        OfflineMaterialRepository(database.materialDao())
    }

    override val clientRepository: ClientRepository by lazy {
        OfflineClientRepository(database.clientDao())
    }

    override val invoiceRepository: InvoiceRepository by lazy {
        OfflineInvoiceRepository(database.invoiceDao())
    }

    override val settingsRepository: SettingsRepository by lazy {
        OfflineSettingsRepository(database.appSettingsDao())
    }

    override val accessRepository: AccessRepository by lazy {
        OfflineAccessRepository(database.accessDao())
    }

    override val paintRepository: PaintRepository by lazy {
        OfflinePaintRepository(database.paintDao(), database.jobPaintDao())
    }

    override val roomRepository: RoomRepository by lazy {
        OfflineRoomRepository(database.roomDao(), database.surfaceDao())
    }
}

