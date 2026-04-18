package com.example.propaintersplastererspayment.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.propaintersplastererspayment.data.local.dao.AccessDao
import com.example.propaintersplastererspayment.data.local.dao.AppSettingsDao
import com.example.propaintersplastererspayment.data.local.dao.ClientDao
import com.example.propaintersplastererspayment.data.local.dao.InvoiceDao
import com.example.propaintersplastererspayment.data.local.dao.JobDao
import com.example.propaintersplastererspayment.data.local.dao.MaterialDao
import com.example.propaintersplastererspayment.data.local.dao.WorkEntryDao
import com.example.propaintersplastererspayment.data.local.entity.AccessItemEntity
import com.example.propaintersplastererspayment.data.local.entity.AppSettingsEntity
import com.example.propaintersplastererspayment.data.local.entity.ClientEntity
import com.example.propaintersplastererspayment.data.local.entity.InvoiceEntity
import com.example.propaintersplastererspayment.data.local.entity.InvoiceLineEntity
import com.example.propaintersplastererspayment.data.local.entity.JobEntity
import com.example.propaintersplastererspayment.data.local.entity.MaterialItemEntity
import com.example.propaintersplastererspayment.data.local.entity.WorkEntryEntity
import com.example.propaintersplastererspayment.data.local.util.Converters

@Database(
    entities = [
        JobEntity::class,
        WorkEntryEntity::class,
        MaterialItemEntity::class,
        ClientEntity::class,
        InvoiceEntity::class,
        InvoiceLineEntity::class,
        AppSettingsEntity::class,
        AccessItemEntity::class
    ],
    version = 16,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun jobDao(): JobDao
    abstract fun workEntryDao(): WorkEntryDao
    abstract fun materialDao(): MaterialDao
    abstract fun clientDao(): ClientDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun appSettingsDao(): AppSettingsDao
    abstract fun accessDao(): AccessDao

    companion object {
        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE jobs ADD COLUMN isQuickInvoice INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Migrate invoices table
                db.execSQL("ALTER TABLE invoices RENAME COLUMN billToName TO billToNameSnapshot")
                db.execSQL("ALTER TABLE invoices RENAME COLUMN issueDate TO invoiceDate")
                db.execSQL("ALTER TABLE invoices RENAME COLUMN includeGst TO gstEnabled")

                // Migrate invoice_lines table
                db.execSQL("ALTER TABLE invoice_lines RENAME COLUMN lineId TO invoiceLineId")
                db.execSQL("ALTER TABLE invoice_lines RENAME COLUMN invoiceOwnerId TO invoiceId")
                db.execSQL("ALTER TABLE invoice_lines RENAME COLUMN isManualAmount TO manualAmountOverride")
            }
        }

        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE jobs ADD COLUMN accessInfo TEXT NOT NULL DEFAULT ''")
            }
        }
        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `access_items` (
                        `accessId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `jobId` INTEGER NOT NULL, 
                        `type` TEXT NOT NULL, 
                        `code` TEXT NOT NULL, 
                        `instructions` TEXT NOT NULL, 
                        FOREIGN KEY(`jobId`) REFERENCES `jobs`(`jobId`) ON UPDATE NO ACTION ON DELETE CASCADE 
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_access_items_jobId` ON `access_items` (`jobId`)")
            }
        }

        val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add new columns to invoices safely
                addColumnIfNotExists(db, "invoices", "billToAddressSnapshot", "TEXT NOT NULL DEFAULT ''")
                addColumnIfNotExists(db, "invoices", "billToPhoneSnapshot", "TEXT NOT NULL DEFAULT ''")
                addColumnIfNotExists(db, "invoices", "billToEmailSnapshot", "TEXT NOT NULL DEFAULT ''")
                
                // Add new column to clients safely
                addColumnIfNotExists(db, "clients", "clientType", "TEXT NOT NULL DEFAULT 'PRIVATE'")
                
                // Add new column to invoice_lines safely
                addColumnIfNotExists(db, "invoice_lines", "sortOrder", "INTEGER NOT NULL DEFAULT 0")
            }

            private fun addColumnIfNotExists(db: SupportSQLiteDatabase, tableName: String, columnName: String, columnDefinition: String) {
                val cursor = db.query("PRAGMA table_info($tableName)")
                var columnExists = false
                while (cursor.moveToNext()) {
                    val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                    if (name == columnName) {
                        columnExists = true
                        break
                    }
                }
                cursor.close()
                if (!columnExists) {
                    db.execSQL("ALTER TABLE $tableName ADD COLUMN $columnName $columnDefinition")
                }
            }
        }
    }
}
