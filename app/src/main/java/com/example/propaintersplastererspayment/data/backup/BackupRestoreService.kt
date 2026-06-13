package com.example.propaintersplastererspayment.data.backup

import android.content.ContentValues
import android.database.Cursor
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.propaintersplastererspayment.data.local.AppDatabase
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class BackupRestoreService(
    private val database: AppDatabase
) {
    suspend fun exportBackup(outputStream: OutputStream): BackupSummary = withContext(Dispatchers.IO) {
        val db = database.openHelper.readableDatabase
        val tables = JSONObject()
        val counts = linkedMapOf<String, Int>()

        BACKUP_TABLES.forEach { table ->
            val rows = db.query("SELECT * FROM $table").use { cursor ->
                cursor.toJsonArray()
            }
            tables.put(table, rows)
            counts[table] = rows.length()
        }

        val backup = JSONObject()
            .put("backupVersion", BACKUP_VERSION)
            .put("databaseVersion", DATABASE_VERSION)
            .put("appName", "ProPaintersPlasterersPayment")
            .put("createdAt", isoTimestamp())
            .put("tables", tables)

        outputStream.bufferedWriter().use { writer ->
            writer.write(backup.toString(2))
        }

        BackupSummary(counts = counts)
    }

    suspend fun inspectBackup(inputStream: InputStream): BackupFileInfo = withContext(Dispatchers.IO) {
        val json = inputStream.bufferedReader().use { it.readText() }.toBackupJson()
        BackupFileInfo(
            backupVersion = json.optInt("backupVersion", -1),
            databaseVersion = json.optInt("databaseVersion", -1),
            createdAt = json.optString("createdAt", "Unknown"),
            counts = json.optJSONObject("tables").toCounts()
        )
    }

    suspend fun restoreBackup(inputStream: InputStream): BackupSummary = withContext(Dispatchers.IO) {
        val json = inputStream.bufferedReader().use { it.readText() }.toBackupJson()
        val backupVersion = json.optInt("backupVersion", -1)
        require(backupVersion == BACKUP_VERSION) {
            "Unsupported backup version: $backupVersion"
        }

        val backupTables = json.optJSONObject("tables")
            ?: throw IllegalArgumentException("Backup file is missing table data.")
        val containsPropertyAccessProfiles = backupTables.has("property_access_profiles")

        val db = database.openHelper.writableDatabase
        val insertedCounts = linkedMapOf<String, Int>()

        db.execSQL("PRAGMA foreign_keys=OFF")
        db.beginTransaction()
        try {
            DELETE_TABLES.forEach { table ->
                db.execSQL("DELETE FROM $table")
            }

            BACKUP_TABLES.forEach { table ->
                val currentColumns = db.currentColumns(table)
                val rows = backupTables.optJSONArray(table) ?: JSONArray()
                for (index in 0 until rows.length()) {
                    val row = rows.getJSONObject(index)
                    db.insert(table, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE, row.toContentValues(currentColumns))
                }
                insertedCounts[table] = rows.length()
            }

            if (!containsPropertyAccessProfiles) {
                AppDatabase.rebuildPropertyAccessProfilesFromJobs(db)
                insertedCounts["property_access_profiles"] =
                    db.rowCount("property_access_profiles")
                insertedCounts["property_access_items"] =
                    db.rowCount("property_access_items")
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            db.execSQL("PRAGMA foreign_keys=ON")
        }

        BackupSummary(counts = insertedCounts)
    }

    private fun String.toBackupJson(): JSONObject {
        val json = try {
            JSONObject(this)
        } catch (e: Exception) {
            throw IllegalArgumentException("This is not a valid JSON backup file.")
        }

        if (!json.has("backupVersion") || !json.has("tables")) {
            throw IllegalArgumentException("This file is not a Pro Painters backup.")
        }
        return json
    }

    private fun Cursor.toJsonArray(): JSONArray {
        val rows = JSONArray()
        while (moveToNext()) {
            val row = JSONObject()
            for (index in 0 until columnCount) {
                val columnName = getColumnName(index)
                when (getType(index)) {
                    Cursor.FIELD_TYPE_NULL -> row.put(columnName, JSONObject.NULL)
                    Cursor.FIELD_TYPE_INTEGER -> row.put(columnName, getLong(index))
                    Cursor.FIELD_TYPE_FLOAT -> row.put(columnName, getDouble(index))
                    Cursor.FIELD_TYPE_STRING -> row.put(columnName, getString(index))
                    Cursor.FIELD_TYPE_BLOB -> row.put(columnName, JSONObject.NULL)
                }
            }
            rows.put(row)
        }
        return rows
    }

    private fun JSONObject?.toCounts(): Map<String, Int> {
        if (this == null) return emptyMap()
        return BACKUP_TABLES.associateWith { table ->
            optJSONArray(table)?.length() ?: 0
        }
    }

    private fun SupportSQLiteDatabase.currentColumns(table: String): Set<String> {
        return query("PRAGMA table_info($table)").use { cursor ->
            buildSet {
                val nameIndex = cursor.getColumnIndex("name")
                while (cursor.moveToNext()) {
                    add(cursor.getString(nameIndex))
                }
            }
        }
    }

    private fun SupportSQLiteDatabase.rowCount(table: String): Int =
        query("SELECT COUNT(*) FROM $table").use { cursor ->
            if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }

    private fun JSONObject.toContentValues(currentColumns: Set<String>): ContentValues {
        val values = ContentValues()
        keys().forEach { key ->
            if (key in currentColumns) {
                when (val value = opt(key)) {
                    null, JSONObject.NULL -> values.putNull(key)
                    is Int -> values.put(key, value)
                    is Long -> values.put(key, value)
                    is Double -> values.put(key, value)
                    is Boolean -> values.put(key, if (value) 1 else 0)
                    else -> values.put(key, value.toString())
                }
            }
        }
        return values
    }

    private fun isoTimestamp(): String =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US).format(Date())

    companion object {
        const val BACKUP_VERSION = 1
        const val DATABASE_VERSION = 29

        val BACKUP_TABLES = listOf(
            "clients",
            "jobs",
            "work_entries",
            "material_items",
            "invoices",
            "invoice_lines",
            "app_settings",
            "access_items",
            "paint_brands",
            "paint_items",
            "job_paints",
            "rooms",
            "surfaces",
            "payments",
            "property_access_profiles",
            "property_access_items"
        )

        private val DELETE_TABLES = listOf(
            "property_access_items",
            "property_access_profiles",
            "payments",
            "surfaces",
            "rooms",
            "job_paints",
            "paint_items",
            "paint_brands",
            "access_items",
            "invoice_lines",
            "invoices",
            "material_items",
            "work_entries",
            "jobs",
            "clients",
            "app_settings"
        )
    }
}

data class BackupSummary(
    val counts: Map<String, Int>
) {
    val totalRows: Int = counts.values.sum()
}

data class BackupFileInfo(
    val backupVersion: Int,
    val databaseVersion: Int,
    val createdAt: String,
    val counts: Map<String, Int>
) {
    val totalRows: Int = counts.values.sum()
}
