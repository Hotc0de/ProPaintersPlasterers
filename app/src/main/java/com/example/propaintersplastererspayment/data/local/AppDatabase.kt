package com.example.propaintersplastererspayment.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.propaintersplastererspayment.data.local.dao.AppSettingsDao
import com.example.propaintersplastererspayment.data.local.dao.ClientDao
import com.example.propaintersplastererspayment.data.local.dao.InvoiceDao
import com.example.propaintersplastererspayment.data.local.dao.JobDao
import com.example.propaintersplastererspayment.data.local.dao.MaterialDao
import com.example.propaintersplastererspayment.data.local.dao.WorkEntryDao
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
        AppSettingsEntity::class
    ],
    version = 12,
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

    companion object {
        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE jobs ADD COLUMN isQuickInvoice INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
