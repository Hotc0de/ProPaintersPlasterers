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
import com.example.propaintersplastererspayment.data.local.dao.JobPaintDao
import com.example.propaintersplastererspayment.data.local.dao.PaintDao
import com.example.propaintersplastererspayment.data.local.entity.JobPaintEntity
import com.example.propaintersplastererspayment.data.local.entity.PaintBrandEntity
import com.example.propaintersplastererspayment.data.local.entity.PaintItemEntity
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
        AccessItemEntity::class,
        PaintBrandEntity::class,
        PaintItemEntity::class,
        JobPaintEntity::class
    ],
    version = 17,
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
    abstract fun paintDao(): PaintDao
    abstract fun jobPaintDao(): JobPaintDao

    companion object {
        val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create paint_brands table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `paint_brands` (
                        `brandId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `brandName` TEXT NOT NULL, 
                        `notes` TEXT NOT NULL DEFAULT '', 
                        `isActive` INTEGER NOT NULL DEFAULT 1
                    )
                """.trimIndent())

                // Create paint_items table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `paint_items` (
                        `paintId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `brandId` INTEGER NOT NULL, 
                        `paintName` TEXT NOT NULL, 
                        `paintCode` TEXT NOT NULL DEFAULT '', 
                        `hexCode` TEXT NOT NULL DEFAULT '', 
                        `finishType` TEXT NOT NULL DEFAULT '', 
                        `notes` TEXT NOT NULL DEFAULT '', 
                        `isArchived` INTEGER NOT NULL DEFAULT 0, 
                        FOREIGN KEY(`brandId`) REFERENCES `paint_brands`(`brandId`) ON UPDATE NO ACTION ON DELETE CASCADE 
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_paint_items_brandId` ON `paint_items` (`brandId`)")

                // Create job_paints table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `job_paints` (
                        `jobPaintId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `jobId` INTEGER NOT NULL, 
                        `paintId` INTEGER NOT NULL, 
                        `notes` TEXT NOT NULL DEFAULT '', 
                        FOREIGN KEY(`jobId`) REFERENCES `jobs`(`jobId`) ON UPDATE NO ACTION ON DELETE CASCADE, 
                        FOREIGN KEY(`paintId`) REFERENCES `paint_items`(`paintId`) ON UPDATE NO ACTION ON DELETE CASCADE 
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_job_paints_jobId` ON `job_paints` (`jobId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_job_paints_paintId` ON `job_paints` (`paintId`)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_job_paints_jobId_paintId` ON `job_paints` (`jobId`, `paintId`)")
                
                // Seed default brands
                db.execSQL("INSERT INTO paint_brands (brandName) VALUES ('Resene')")
                db.execSQL("INSERT INTO paint_brands (brandName) VALUES ('Dulux')")
                db.execSQL("INSERT INTO paint_brands (brandName) VALUES ('Wattyl')")
            }
        }
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
                addColumnSafely(db, "invoices", "billToAddressSnapshot", "TEXT NOT NULL DEFAULT ''")
                addColumnSafely(db, "invoices", "billToPhoneSnapshot", "TEXT NOT NULL DEFAULT ''")
                addColumnSafely(db, "invoices", "billToEmailSnapshot", "TEXT NOT NULL DEFAULT ''")
                addColumnSafely(db, "clients", "clientType", "TEXT NOT NULL DEFAULT 'PRIVATE'")
                addColumnSafely(db, "invoice_lines", "sortOrder", "INTEGER NOT NULL DEFAULT 0")
            }

            private fun addColumnSafely(db: SupportSQLiteDatabase, tableName: String, columnName: String, columnDefinition: String) {
                try {
                    db.execSQL("ALTER TABLE $tableName ADD COLUMN $columnName $columnDefinition")
                } catch (e: Exception) {
                    // Column likely already exists, ignore.
                }
            }
        }
    }
}
