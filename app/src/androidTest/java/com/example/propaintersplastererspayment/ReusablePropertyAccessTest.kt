package com.example.propaintersplastererspayment

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.propaintersplastererspayment.data.backup.BackupRestoreService
import com.example.propaintersplastererspayment.data.local.AppDatabase
import com.example.propaintersplastererspayment.data.local.entity.AccessItemEntity
import com.example.propaintersplastererspayment.data.local.entity.JobEntity
import com.example.propaintersplastererspayment.data.repository.OfflineAccessRepository
import com.example.propaintersplastererspayment.data.repository.OfflineJobRepository
import java.io.ByteArrayInputStream
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReusablePropertyAccessTest {

    private lateinit var database: AppDatabase
    private lateinit var jobRepository: OfflineJobRepository
    private lateinit var accessRepository: OfflineAccessRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        jobRepository = OfflineJobRepository(database.jobDao())
        accessRepository = OfflineAccessRepository(database.accessDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun accessProfileCopiesToFutureJobsAndCanBeCleared() = runBlocking {
        val firstJobId = jobRepository.saveJob(
            JobEntity(propertyAddress = "123   TTT", clientNameSnapshot = "Trung")
        )
        accessRepository.saveAccessItem(
            AccessItemEntity(
                jobId = firstJobId,
                type = "Alarm Code",
                code = "4567",
                instructions = "Disarm on entry"
            )
        )

        val secondJobId = jobRepository.saveJob(
            JobEntity(propertyAddress = "  123 ttt  ", clientNameSnapshot = "Trung")
        )
        val copiedItems = database.accessDao().getAccessItems(secondJobId)

        assertEquals(1, copiedItems.size)
        assertEquals("4567", copiedItems.single().code)

        accessRepository.deleteAccessItem(copiedItems.single())

        val thirdJobId = jobRepository.saveJob(
            JobEntity(propertyAddress = "123 TTT", clientNameSnapshot = "Trung")
        )
        assertEquals(emptyList<AccessItemEntity>(), database.accessDao().getAccessItems(thirdJobId))

        val originalItems = database.accessDao().getAccessItems(firstJobId)
        assertEquals("4567", originalItems.single().code)
    }

    @Test
    fun restoringLegacyBackupRebuildsReusablePropertyProfiles() = runBlocking {
        val legacyBackup = """
            {
              "backupVersion": 1,
              "databaseVersion": 28,
              "createdAt": "2026-06-13T18:00:00+1200",
              "tables": {
                "jobs": [
                  {
                    "jobId": 10,
                    "propertyAddress": "45 Example Road",
                    "clientId": null,
                    "clientNameSnapshot": "Client",
                    "jobName": "Old job",
                    "notes": "",
                    "status": "FINISHED",
                    "createdAt": 1000,
                    "startDateOverride": null,
                    "finishDateOverride": null,
                    "isQuickInvoice": 0,
                    "accessInfo": "",
                    "jobType": "PRIVATE"
                  }
                ],
                "access_items": [
                  {
                    "accessId": 20,
                    "jobId": 10,
                    "type": "Lockbox",
                    "code": "2468",
                    "instructions": "Beside the garage"
                  }
                ]
              }
            }
        """.trimIndent()

        BackupRestoreService(database).restoreBackup(
            ByteArrayInputStream(legacyBackup.toByteArray())
        )

        val newJobId = jobRepository.saveJob(
            JobEntity(propertyAddress = " 45 example road ", clientNameSnapshot = "Client")
        )
        val copiedItems = database.accessDao().getAccessItems(newJobId)

        assertEquals(1, copiedItems.size)
        assertEquals("2468", copiedItems.single().code)
        assertEquals("Beside the garage", copiedItems.single().instructions)
    }
}
