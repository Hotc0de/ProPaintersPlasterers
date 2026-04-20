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
import com.example.propaintersplastererspayment.data.local.dao.RoomDao
import com.example.propaintersplastererspayment.data.local.dao.SurfaceDao
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
import com.example.propaintersplastererspayment.data.local.entity.RoomEntity
import com.example.propaintersplastererspayment.data.local.entity.SurfaceEntity
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
        JobPaintEntity::class,
        RoomEntity::class,
        SurfaceEntity::class
    ],
    version = 19,
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
    abstract fun roomDao(): RoomDao
    abstract fun surfaceDao(): SurfaceDao

    companion object {
        val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create rooms table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `rooms` (
                        `roomId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `jobId` INTEGER NOT NULL, 
                        `roomType` TEXT NOT NULL, 
                        `roomName` TEXT NOT NULL, 
                        `level` TEXT, 
                        `notes` TEXT, 
                        `sortOrder` INTEGER NOT NULL, 
                        `createdAt` INTEGER NOT NULL, 
                        `updatedAt` INTEGER NOT NULL, 
                        FOREIGN KEY(`jobId`) REFERENCES `jobs`(`jobId`) ON UPDATE NO ACTION ON DELETE CASCADE 
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_rooms_jobId` ON `rooms` (`jobId`)")

                // Create surfaces table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `surfaces` (
                        `surfaceId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `roomId` INTEGER NOT NULL, 
                        `surfaceType` TEXT NOT NULL, 
                        `surfaceLabel` TEXT NOT NULL, 
                        `selectedJobPaintId` INTEGER, 
                        `finishTypeOverride` TEXT, 
                        `coatCount` INTEGER NOT NULL, 
                        `isFeatureSurface` INTEGER NOT NULL, 
                        `notes` TEXT, 
                        `sortOrder` INTEGER NOT NULL, 
                        `surfaceCount` INTEGER NOT NULL, 
                        `areaSize` REAL, 
                        `areaUnit` TEXT, 
                        FOREIGN KEY(`roomId`) REFERENCES `rooms`(`roomId`) ON UPDATE NO ACTION ON DELETE CASCADE, 
                        FOREIGN KEY(`selectedJobPaintId`) REFERENCES `job_paints`(`jobPaintId`) ON UPDATE NO ACTION ON DELETE SET NULL 
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_surfaces_roomId` ON `surfaces` (`roomId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_surfaces_selectedJobPaintId` ON `surfaces` (`selectedJobPaintId`)")
            }
        }

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

        val MIGRATION_18_19 = object : Migration(18, 19) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Rename roomName to customName and add displayName
                db.execSQL("ALTER TABLE rooms RENAME COLUMN roomName TO customName")
                db.execSQL("ALTER TABLE rooms ADD COLUMN displayName TEXT NOT NULL DEFAULT ''")
                // Remove level column (not needed for Room step)
                db.execSQL("""
                    CREATE TABLE rooms_new (
                        roomId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        jobId INTEGER NOT NULL, 
                        roomType TEXT NOT NULL, 
                        customName TEXT NOT NULL, 
                        displayName TEXT NOT NULL, 
                        notes TEXT, 
                        sortOrder INTEGER NOT NULL, 
                        createdAt INTEGER NOT NULL, 
                        updatedAt INTEGER NOT NULL, 
                        FOREIGN KEY(`jobId`) REFERENCES `jobs`(`jobId`) ON UPDATE NO ACTION ON DELETE CASCADE 
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO rooms_new (roomId, jobId, roomType, customName, displayName, notes, sortOrder, createdAt, updatedAt)
                    SELECT roomId, jobId, roomType, customName, customName, notes, sortOrder, createdAt, updatedAt FROM rooms
                """.trimIndent())
                db.execSQL("DROP TABLE rooms")
                db.execSQL("ALTER TABLE rooms_new RENAME TO rooms")
                db.execSQL("CREATE INDEX `index_rooms_jobId` ON `rooms` (`jobId`)")
            }
        }
    }
}
