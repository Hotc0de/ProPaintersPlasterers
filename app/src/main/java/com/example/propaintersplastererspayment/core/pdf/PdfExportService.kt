package com.example.propaintersplastererspayment.core.pdf

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.propaintersplastererspayment.core.util.CurrencyFormatUtils
import com.example.propaintersplastererspayment.core.util.WorkEntryTimeUtils
import java.io.File
import java.io.FileOutputStream

class PdfExportService {

    private data class PageCursor(
        var document: PdfDocument,
        var page: PdfDocument.Page,
        var pageNumber: Int,
        var y: Float
    )

    private val pageWidth = 595
    private val pageHeight = 842
    private val margin = 40f

    fun exportTimesheetPdf(context: Context, data: TimesheetPdfData, outputFile: File): File {
        val document = PdfDocument()
        val normalPaint = textPaint(11f)
        val boldPaint = textPaint(11f, true)
        val titlePaint = textPaint(18f, true)
        val smallPaint = textPaint(9f)
        val linePaint = Paint().apply { strokeWidth = 1f }

        val cursor = startDocument(document)

        cursor.y = drawBusinessHeader(
            cursor,
            data.business,
            "Timesheet",
            titlePaint,
            normalPaint,
            linePaint
        )

        cursor.y += 8f
        drawLine(cursor, linePaint)
        cursor.y += 18f

        cursor.y = drawLabelValue(cursor, "Job", data.jobName.ifBlank { "Unnamed Job" }, boldPaint, normalPaint)
        cursor.y = drawLabelValue(cursor, "Address", data.jobAddress, boldPaint, normalPaint)
        cursor.y = drawLabelValue(cursor, "Exported", data.exportedAt, boldPaint, normalPaint)

        cursor.y += 12f
        drawSectionTitle(cursor, "Work Entries", boldPaint)
        cursor.y += 6f
        cursor.y = drawWorkEntryTableHeader(cursor, boldPaint, linePaint)

        data.workEntries.forEach { row ->
            ensureSpace(cursor, 18f) {
                drawWorkEntryTableHeader(cursor, boldPaint, linePaint)
            }
            val canvas = cursor.page.canvas
            val x0 = margin
            val x1 = 150f
            val x2 = 285f
            val x3 = 360f
            val x4 = 435f
            val rowY = cursor.y
            canvas.drawText(row.workDate, x0, rowY, normalPaint)
            canvas.drawText(row.workerName, x1, rowY, normalPaint)
            canvas.drawText(row.startTime, x2, rowY, normalPaint)
            canvas.drawText(row.finishTime, x3, rowY, normalPaint)
            canvas.drawText(WorkEntryTimeUtils.formatHours(row.hoursWorked), x4, rowY, normalPaint)
            cursor.y += 16f
        }

        cursor.y += 4f
        drawLine(cursor, linePaint)
        cursor.y += 16f
        cursor.y = drawRightValue(
            cursor,
            "Total Hours: ${WorkEntryTimeUtils.formatHours(data.totalHours)}",
            boldPaint
        )

        cursor.y += 18f
        ensureSpace(cursor, 80f)
        drawSectionTitle(cursor, "Materials", boldPaint)
        cursor.y += 6f
        cursor.y = drawMaterialsTableHeader(cursor, boldPaint, linePaint)

        data.materials.forEach { row ->
            ensureSpace(cursor, 18f) {
                drawMaterialsTableHeader(cursor, boldPaint, linePaint)
            }
            val canvas = cursor.page.canvas
            val rowY = cursor.y
            canvas.drawText(row.materialName, margin, rowY, normalPaint)
            canvas.drawText(CurrencyFormatUtils.formatCurrency(row.price), 430f, rowY, normalPaint)
            cursor.y += 16f
        }

        cursor.y += 4f
        drawLine(cursor, linePaint)
        cursor.y += 16f
        cursor.y = drawRightValue(
            cursor,
            "Total Material Cost: ${CurrencyFormatUtils.formatCurrency(data.totalMaterialCost)}",
            boldPaint
        )

        drawFooter(cursor, smallPaint)
        finishDocument(cursor)
        writeDocument(document, outputFile)
        return outputFile
    }

    fun exportInvoicePdf(context: Context, data: InvoicePdfData, outputFile: File): File {
        val document = PdfDocument()
        val normalPaint = textPaint(11f)
        val boldPaint = textPaint(11f, true)
        val titlePaint = textPaint(18f, true)
        val smallPaint = textPaint(9f)
        val linePaint = Paint().apply { strokeWidth = 1f }

        val cursor = startDocument(document)

        cursor.y = drawBusinessHeader(
            cursor,
            data.business,
            "Invoice",
            titlePaint,
            normalPaint,
            linePaint
        )

        cursor.y += 8f
        drawLine(cursor, linePaint)
        cursor.y += 18f

        cursor.y = drawLabelValue(cursor, "Invoice Number", data.invoiceNumber, boldPaint, normalPaint)
        cursor.y = drawLabelValue(cursor, "Invoice Date", data.issueDate, boldPaint, normalPaint)
        cursor.y = drawLabelValue(cursor, "Bill To", data.billToName, boldPaint, normalPaint)
        cursor.y = drawLabelValue(cursor, "Job", data.jobName.ifBlank { "Unnamed Job" }, boldPaint, normalPaint)
        cursor.y = drawLabelValue(cursor, "Job Address", data.jobAddress, boldPaint, normalPaint)

        cursor.y += 12f
        drawSectionTitle(cursor, "Invoice Lines", boldPaint)
        cursor.y += 6f
        cursor.y = drawInvoiceTableHeader(cursor, boldPaint, linePaint)

        data.lines.forEach { row ->
            ensureSpace(cursor, 18f) {
                drawInvoiceTableHeader(cursor, boldPaint, linePaint)
            }
            val canvas = cursor.page.canvas
            val rowY = cursor.y
            canvas.drawText(row.description, margin, rowY, normalPaint)
            canvas.drawText(formatDecimal(row.qty), 300f, rowY, normalPaint)
            canvas.drawText(CurrencyFormatUtils.formatCurrency(row.rate), 360f, rowY, normalPaint)
            canvas.drawText(CurrencyFormatUtils.formatCurrency(row.amount), 470f, rowY, normalPaint)
            cursor.y += 16f
        }

        cursor.y += 4f
        drawLine(cursor, linePaint)
        cursor.y += 18f

        cursor.y = drawTotalRow(cursor, "Subtotal (excl GST)", data.subtotalExGst, boldPaint, normalPaint)
        if (data.includeGst) {
            val gstLabel = "GST (${(data.gstRate * 100).toInt()}%)"
            cursor.y = drawTotalRow(cursor, gstLabel, data.gstAmount, boldPaint, normalPaint)
            cursor.y = drawTotalRow(cursor, "Subtotal (incl GST)", data.totalIncGst, boldPaint, normalPaint)
        }
        if (data.otherAmount != 0.0) {
            cursor.y = drawTotalRow(cursor, "Other Amount", data.otherAmount, boldPaint, normalPaint)
        }

        cursor.y += 4f
        drawLine(cursor, linePaint)
        cursor.y += 16f
        cursor.y = drawTotalRow(cursor, "Final Total", data.finalTotal, boldPaint, boldPaint)

        if (data.notes.isNotBlank()) {
            cursor.y += 16f
            ensureSpace(cursor, 40f)
            drawSectionTitle(cursor, "Notes", boldPaint)
            cursor.y += 14f
            cursor.page.canvas.drawText(data.notes, margin, cursor.y, normalPaint)
            cursor.y += 14f
        }

        drawFooter(cursor, smallPaint)
        finishDocument(cursor)
        writeDocument(document, outputFile)
        return outputFile
    }

    private fun textPaint(textSize: Float, isBold: Boolean = false): Paint =
        Paint().apply {
            this.textSize = textSize
            isAntiAlias = true
            typeface = if (isBold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.DEFAULT
        }

    private fun startDocument(document: PdfDocument): PageCursor {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        return PageCursor(document = document, page = page, pageNumber = 1, y = margin)
    }

    private fun finishDocument(cursor: PageCursor) {
        cursor.document.finishPage(cursor.page)
    }

    private fun writeDocument(document: PdfDocument, outputFile: File) {
        outputFile.parentFile?.mkdirs()
        FileOutputStream(outputFile).use { stream ->
            document.writeTo(stream)
        }
        document.close()
    }

    private fun ensureSpace(
        cursor: PageCursor,
        requiredHeight: Float,
        onNewPageHeader: (() -> Unit)? = null
    ) {
        val bottomLimit = pageHeight - margin - 30f
        if (cursor.y + requiredHeight <= bottomLimit) return

        drawFooter(cursor, textPaint(9f))
        cursor.document.finishPage(cursor.page)
        cursor.pageNumber += 1
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, cursor.pageNumber).create()
        cursor.page = cursor.document.startPage(pageInfo)
        cursor.y = margin
        onNewPageHeader?.invoke()
    }

    private fun drawBusinessHeader(
        cursor: PageCursor,
        details: PdfBusinessDetails,
        documentTitle: String,
        titlePaint: Paint,
        normalPaint: Paint,
        linePaint: Paint
    ): Float {
        val canvas = cursor.page.canvas
        canvas.drawText(details.businessName.ifBlank { "Business" }, margin, cursor.y, titlePaint)
        canvas.drawText(documentTitle, pageWidth - 160f, cursor.y, titlePaint)

        var y = cursor.y + 18f
        if (details.address.isNotBlank()) {
            canvas.drawText(details.address, margin, y, normalPaint)
            y += 14f
        }
        if (details.phoneNumber.isNotBlank()) {
            canvas.drawText("Phone: ${details.phoneNumber}", margin, y, normalPaint)
            y += 14f
        }
        if (details.email.isNotBlank()) {
            canvas.drawText("Email: ${details.email}", margin, y, normalPaint)
            y += 14f
        }
        if (details.gstNumber.isNotBlank()) {
            canvas.drawText("GST: ${details.gstNumber}", margin, y, normalPaint)
            y += 14f
        }
        if (details.bankAccountNumber.isNotBlank()) {
            canvas.drawText("Bank: ${details.bankAccountNumber}", margin, y, normalPaint)
            y += 14f
        }

        canvas.drawLine(margin, y, pageWidth - margin, y, linePaint)
        return y + 10f
    }

    private fun drawSectionTitle(cursor: PageCursor, title: String, paint: Paint) {
        cursor.page.canvas.drawText(title, margin, cursor.y, paint)
    }

    private fun drawLabelValue(
        cursor: PageCursor,
        label: String,
        value: String,
        labelPaint: Paint,
        valuePaint: Paint
    ): Float {
        ensureSpace(cursor, 16f)
        cursor.page.canvas.drawText("$label:", margin, cursor.y, labelPaint)
        cursor.page.canvas.drawText(value, margin + 120f, cursor.y, valuePaint)
        return cursor.y + 15f
    }

    private fun drawRightValue(cursor: PageCursor, value: String, paint: Paint): Float {
        ensureSpace(cursor, 16f)
        cursor.page.canvas.drawText(value, 360f, cursor.y, paint)
        return cursor.y + 15f
    }

    private fun drawTotalRow(
        cursor: PageCursor,
        label: String,
        amount: Double,
        labelPaint: Paint,
        valuePaint: Paint
    ): Float {
        ensureSpace(cursor, 16f)
        cursor.page.canvas.drawText(label, 320f, cursor.y, labelPaint)
        cursor.page.canvas.drawText(CurrencyFormatUtils.formatCurrency(amount), 470f, cursor.y, valuePaint)
        return cursor.y + 15f
    }

    private fun drawWorkEntryTableHeader(cursor: PageCursor, paint: Paint, linePaint: Paint): Float {
        val canvas = cursor.page.canvas
        ensureSpace(cursor, 20f)
        val y = cursor.y
        canvas.drawText("Date", margin, y, paint)
        canvas.drawText("Worker", 150f, y, paint)
        canvas.drawText("Start", 285f, y, paint)
        canvas.drawText("Finish", 360f, y, paint)
        canvas.drawText("Hours", 435f, y, paint)
        canvas.drawLine(margin, y + 4f, pageWidth - margin, y + 4f, linePaint)
        return y + 16f
    }

    private fun drawMaterialsTableHeader(cursor: PageCursor, paint: Paint, linePaint: Paint): Float {
        val canvas = cursor.page.canvas
        ensureSpace(cursor, 20f)
        val y = cursor.y
        canvas.drawText("Material", margin, y, paint)
        canvas.drawText("Price", 430f, y, paint)
        canvas.drawLine(margin, y + 4f, pageWidth - margin, y + 4f, linePaint)
        return y + 16f
    }

    private fun drawInvoiceTableHeader(cursor: PageCursor, paint: Paint, linePaint: Paint): Float {
        val canvas = cursor.page.canvas
        ensureSpace(cursor, 20f)
        val y = cursor.y
        canvas.drawText("Description", margin, y, paint)
        canvas.drawText("Qty", 300f, y, paint)
        canvas.drawText("Rate", 360f, y, paint)
        canvas.drawText("Amount", 470f, y, paint)
        canvas.drawLine(margin, y + 4f, pageWidth - margin, y + 4f, linePaint)
        return y + 16f
    }

    private fun drawLine(cursor: PageCursor, paint: Paint) {
        cursor.page.canvas.drawLine(margin, cursor.y, pageWidth - margin, cursor.y, paint)
    }

    private fun drawFooter(cursor: PageCursor, paint: Paint) {
        val footerY = pageHeight - margin
        val text = "Generated by ProPainters app - Page ${cursor.pageNumber}"
        cursor.page.canvas.drawText(text, margin, footerY, paint)
    }

    private fun formatDecimal(value: Double): String =
        if (value % 1.0 == 0.0) value.toInt().toString() else "%.2f".format(value)
}

