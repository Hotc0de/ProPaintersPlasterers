package com.example.propaintersplastererspayment.core.pdf

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object PdfFileHelper {

    fun createExportFile(context: Context, fileName: String): File {
        val safeName = if (fileName.endsWith(".pdf")) fileName else "$fileName.pdf"
        val root = context.getExternalFilesDir("Documents") ?: context.filesDir
        val exportDir = File(root, "exports")
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }
        // If a file with the same name already exists, remove it so previews
        // are always freshly generated (prevents stale copies / viewer cache
        // confusion when the same name is reused in debug flows).
        val out = File(exportDir, safeName)
        if (out.exists()) {
            try { out.delete() } catch (_: Throwable) { /* ignore */ }
        }
        return out
    }

    /**
     * Delete all exported preview files created by the app. Useful in debug
     * flows to clear previous previews and avoid confusion from cached files.
     */
    fun clearExportCache(context: Context) {
        try {
            val root = context.getExternalFilesDir("Documents") ?: context.filesDir
            val exportDir = File(root, "exports")
            if (exportDir.exists()) {
                exportDir.listFiles()?.forEach { f ->
                    if (f.isFile && f.name.endsWith(".pdf")) {
                        try { f.delete() } catch (_: Throwable) { /* ignore */ }
                    }
                }
            }
        } catch (_: Throwable) { /* ignore */ }
    }

    fun getUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun sharePdf(context: Context, file: File, chooserTitle: String) {
        val uri = getUri(context, file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, chooserTitle))
    }

    fun openPdf(context: Context, file: File): Boolean {
        val uri = getUri(context, file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            false
        }
    }
}

