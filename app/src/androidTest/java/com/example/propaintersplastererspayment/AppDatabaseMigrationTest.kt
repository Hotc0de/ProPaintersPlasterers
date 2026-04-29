package com.example.propaintersplastererspayment

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.propaintersplastererspayment.data.local.AppDatabase
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
    fun migrate11ToLatest_preservesSchemaIntegrity() {
        helper.createDatabase(testDbName, 11).apply {
            close()
        }

        helper.runMigrationsAndValidate(
            testDbName,
            24,
            true,
            *AppDatabase.ALL_MIGRATIONS
        )
    }
}


