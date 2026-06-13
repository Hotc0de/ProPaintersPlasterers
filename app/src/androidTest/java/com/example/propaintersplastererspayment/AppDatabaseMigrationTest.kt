package com.example.propaintersplastererspayment

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.propaintersplastererspayment.data.local.AppDatabase
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {

    private val testDbName = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate23ToLatest_preservesSchemaIntegrity() {
        helper.createDatabase(testDbName, 23).apply {
            close()
        }

        helper.runMigrationsAndValidate(
            testDbName,
            29,
            true,
            *AppDatabase.ALL_MIGRATIONS
        )
    }

    @Test
    fun migrate28To29_preservesJobsAndAccessWhileCreatingReusableProfiles() {
        helper.createDatabase(testDbName, 28).apply {
            execSQL(
                """
                INSERT INTO jobs (
                    jobId, propertyAddress, clientId, clientNameSnapshot, jobName,
                    notes, status, createdAt, startDateOverride, finishDateOverride,
                    isQuickInvoice, accessInfo, jobType
                ) VALUES (
                    101, ' 123   TTT ', NULL, 'Trung', 'First visit',
                    '', 'FINISHED', 1000, NULL, NULL,
                    0, '', 'PRIVATE'
                )
                """.trimIndent()
            )
            execSQL(
                """
                INSERT INTO access_items (accessId, jobId, type, code, instructions)
                VALUES (201, 101, 'Alarm Code', '4567', 'Disarm on entry')
                """.trimIndent()
            )
            execSQL(
                """
                INSERT INTO jobs (
                    jobId, propertyAddress, clientId, clientNameSnapshot, jobName,
                    notes, status, createdAt, startDateOverride, finishDateOverride,
                    isQuickInvoice, accessInfo, jobType
                ) VALUES (
                    102, '123 ttt', NULL, 'Trung', 'Second visit',
                    '', 'WORKING', 2000, NULL, NULL,
                    0, '', 'PRIVATE'
                )
                """.trimIndent()
            )
            execSQL(
                """
                INSERT INTO access_items (accessId, jobId, type, code, instructions)
                VALUES (202, 102, 'Alarm Code', '9999', 'Latest code')
                """.trimIndent()
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(
            testDbName,
            29,
            true,
            AppDatabase.MIGRATION_28_29
        )

        db.query("SELECT COUNT(*) FROM jobs").use { cursor ->
            cursor.moveToFirst()
            assertEquals(2, cursor.getInt(0))
        }
        db.query("SELECT COUNT(*) FROM access_items").use { cursor ->
            cursor.moveToFirst()
            assertEquals(2, cursor.getInt(0))
        }
        db.query(
            """
            SELECT p.addressKey, i.code
            FROM property_access_profiles AS p
            JOIN property_access_items AS i ON i.profileId = p.profileId
            """.trimIndent()
        ).use { cursor ->
            cursor.moveToFirst()
            assertEquals("123 ttt", cursor.getString(0))
            assertEquals("9999", cursor.getString(1))
            assertEquals(1, cursor.count)
        }
        db.close()
    }
}

