package com.example.propaintersplastererspayment.core.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import com.example.propaintersplastererspayment.core.util.CurrencyFormatUtils
import com.example.propaintersplastererspayment.core.util.DateFormatUtils
import com.example.propaintersplastererspayment.core.util.WorkEntryTimeUtils
import com.example.propaintersplastererspayment.feature.invoice.mapper.InvoiceDataMapper
import com.example.propaintersplastererspayment.feature.invoice.ui.luxury.InvoiceData
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

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
        cursor.y = drawLabelValue(
            cursor,
            "Exported",
            DateFormatUtils.formatDisplayDate(data.exportedAt),
            boldPaint,
            normalPaint
        )

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
            canvas.drawText(DateFormatUtils.formatDisplayDate(row.workDate), x0, rowY, normalPaint)
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
        return exportInvoicePdfLuxury(data, outputFile)
    }

    private fun exportInvoicePdfLuxury(data: InvoicePdfData, outputFile: File): File {
        val invoiceData = InvoiceDataMapper.map(data)
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        drawLuxuryInvoicePage(canvas = canvas, invoiceData = invoiceData)

        document.finishPage(page)
        writeDocument(document, outputFile)
        return outputFile
    }

    private fun drawLuxuryInvoicePage(canvas: Canvas, invoiceData: InvoiceData) {
        val offWhite = Color.parseColor("#FDFCFB")
        val darkSlate = Color.parseColor("#1E293B")
        val darkSlate2 = Color.parseColor("#334155")
        val bronze = Color.parseColor("#9D8560")
        val lightGray1 = Color.parseColor("#F7F5F2")
        val lightGray2 = Color.parseColor("#F1F5F9")
        val lightGray3 = Color.parseColor("#E2E8F0")
        val mediumGray = Color.parseColor("#94A3B8")
        val darkGray = Color.parseColor("#475569")
        val textGray = Color.parseColor("#64748B")
        val white = Color.WHITE

        val contentLeft = margin
        val contentTop = margin
        val contentRight = pageWidth - margin
        val contentWidth = contentRight - contentLeft
        var y = contentTop

        val fillPaint = Paint().apply { style = Paint.Style.FILL; color = offWhite }
        val accentPaint = Paint().apply { style = Paint.Style.FILL; color = bronze }
        val borderPaint = Paint().apply { style = Paint.Style.STROKE; color = bronze; strokeWidth = 1.2f }
        val subtleBorderPaint = Paint().apply { style = Paint.Style.STROKE; color = lightGray3; strokeWidth = 1f }
        val darkFillPaint = Paint().apply { style = Paint.Style.FILL; color = darkSlate }
        val darkFillPaint2 = Paint().apply { style = Paint.Style.FILL; color = darkSlate2 }
        val whiteFillPaint = Paint().apply { style = Paint.Style.FILL; color = white }

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = darkSlate
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val hugeTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = darkSlate
            textSize = 42f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val sectionLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = mediumGray
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = darkGray
            textSize = 10.5f
        }
        val bodyBoldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = darkSlate
            textSize = 11.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val smallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textGray
            textSize = 9f
        }
        val whiteTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = white
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val whiteAmountPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = white
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val bronzeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bronze
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        canvas.drawRect(contentLeft, contentTop, contentRight, pageHeight - margin, fillPaint)
        canvas.drawRect(contentLeft, y, contentRight, y + 3f, accentPaint)
        y += 14f

        // Header card
        val headerHeight = 150f
        val headerRect = RectF(contentLeft, y, contentRight, y + headerHeight)
        canvas.drawRoundRect(headerRect, 4f, 4f, whiteFillPaint)
        canvas.drawRoundRect(headerRect, 4f, 4f, borderPaint)

        // Left brand area
        val badgeRect = RectF(contentLeft + 14f, y + 14f, contentLeft + 54f, y + 54f)
        canvas.drawRoundRect(badgeRect, 3f, 3f, darkFillPaint)
        canvas.drawText("PPP", badgeRect.left + 8f, badgeRect.top + 25f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bronze
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        })

        canvas.drawText(invoiceData.businessName, contentLeft + 68f, y + 34f, titlePaint)
        canvas.drawText(invoiceData.businessSubtitle, contentLeft + 68f, y + 52f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bronze
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        })

        drawWrappedText(canvas, "ADDRESS", invoiceData.businessAddress, contentLeft + 16f, y + 74f, 150f, sectionLabelPaint, bodyPaint)
        drawWrappedText(canvas, "CONTACT", "${invoiceData.businessPhone}\n${invoiceData.businessEmail}", contentLeft + 170f, y + 74f, 150f, sectionLabelPaint, bodyPaint)
        drawWrappedText(canvas, "ACCOUNT", invoiceData.accountNumber, contentLeft + 324f, y + 74f, 120f, sectionLabelPaint, bodyPaint)

        canvas.drawText("INVOICE", contentRight - 150f, y + 52f, hugeTitlePaint)
        y += headerHeight + 14f

        // Detail cards
        val cardGap = 12f
        val cardWidth = (contentWidth - cardGap) / 2f
        val leftCard = RectF(contentLeft, y, contentLeft + cardWidth, y + 90f)
        val rightCard = RectF(contentLeft + cardWidth + cardGap, y, contentRight, y + 90f)
        canvas.drawRoundRect(leftCard, 4f, 4f, whiteFillPaint)
        canvas.drawRoundRect(rightCard, 4f, 4f, whiteFillPaint)
        canvas.drawRoundRect(leftCard, 4f, 4f, subtleBorderPaint)
        canvas.drawRoundRect(rightCard, 4f, 4f, subtleBorderPaint)

        drawCardLabelValue(canvas, leftCard.left + 12f, leftCard.top + 20f, "INVOICE NUMBER", invoiceData.invoiceNumber, sectionLabelPaint, bodyBoldPaint)
        drawCardLabelValue(canvas, leftCard.left + 12f, leftCard.top + 48f, "ISSUE DATE", invoiceData.issueDate, sectionLabelPaint, bodyPaint)
        drawCardLabelValue(canvas, leftCard.left + 130f, leftCard.top + 48f, "DUE DATE", invoiceData.dueDate, sectionLabelPaint, Paint(bodyBoldPaint).apply { color = bronze })

        drawCardLabelValue(canvas, rightCard.left + 12f, rightCard.top + 20f, "BILL TO", invoiceData.billTo, sectionLabelPaint, bodyBoldPaint)
        drawWrappedText(canvas, "JOB ADDRESS", invoiceData.jobAddress, rightCard.left + 12f, rightCard.top + 48f, cardWidth - 24f, sectionLabelPaint, bodyPaint)
        y += 108f

        // Services header
        canvas.drawText("SERVICES & MATERIALS", contentLeft + 20f, y, bodyBoldPaint)
        y += 12f

        val tableHeaderHeight = 24f
        canvas.drawRect(contentLeft, y, contentRight, y + tableHeaderHeight, darkFillPaint2)
        drawTableHeaderText(canvas, "DESCRIPTION", contentLeft + 12f, y + 16f, whiteTextPaint)
        drawTableHeaderText(canvas, "QTY", contentLeft + 320f, y + 16f, whiteTextPaint)
        drawTableHeaderText(canvas, "UNIT PRICE", contentLeft + 390f, y + 16f, whiteTextPaint)
        drawTableHeaderText(canvas, "LINE TOTAL", contentLeft + 485f, y + 16f, whiteTextPaint)
        y += tableHeaderHeight

        invoiceData.lineItems.forEachIndexed { index, item ->
            val rowHeight = 28f
            fillPaint.color = if (index % 2 == 0) white else lightGray1
            canvas.drawRect(contentLeft, y, contentRight, y + rowHeight, fillPaint)
            canvas.drawLine(contentLeft, y + rowHeight, contentRight, y + rowHeight, subtleBorderPaint)
            canvas.drawText(item.description.take(45), contentLeft + 12f, y + 18f, bodyPaint)
            canvas.drawText(item.quantity.toString(), contentLeft + 328f, y + 18f, bodyPaint)
            canvas.drawText(CurrencyFormatUtils.formatCurrency(item.rate), contentLeft + 390f, y + 18f, bodyPaint)
            canvas.drawText(CurrencyFormatUtils.formatCurrency(item.amount), contentLeft + 485f, y + 18f, bodyBoldPaint)
            y += rowHeight
        }

        y += 14f

        // Totals cards
        val totalsWidth = 250f
        val totalsLeft = contentRight - totalsWidth
        val totalsCard = RectF(totalsLeft, y, contentRight, y + 86f)
        canvas.drawRoundRect(totalsCard, 4f, 4f, whiteFillPaint)
        canvas.drawRoundRect(totalsCard, 4f, 4f, subtleBorderPaint)
        drawAmountRow(canvas, "SUBTOTAL", invoiceData.subtotal, totalsLeft + 12f, y + 18f, sectionLabelPaint, bodyPaint)
        drawAmountRow(canvas, "OTHER AMOUNT", invoiceData.otherAmount, totalsLeft + 12f, y + 40f, sectionLabelPaint, bodyPaint)
        drawAmountRow(canvas, "GST", invoiceData.subtotal * invoiceData.gstRate, totalsLeft + 12f, y + 62f, sectionLabelPaint, bodyPaint)
        y += 96f

        val total = invoiceData.subtotal + invoiceData.otherAmount + (invoiceData.subtotal * invoiceData.gstRate)
        val dueCard = RectF(totalsLeft, y, contentRight, y + 74f)
        canvas.drawRoundRect(dueCard, 4f, 4f, darkFillPaint)
        canvas.drawRoundRect(dueCard, 4f, 4f, borderPaint)
        canvas.drawText("AMOUNT DUE", totalsLeft + 12f, y + 24f, bronzeTextPaint)
        canvas.drawText("Due: ${invoiceData.dueDate}", totalsLeft + 12f, y + 40f, smallPaint.apply { color = Color.parseColor("#CBD5E1") })
        canvas.drawText(CurrencyFormatUtils.formatCurrency(total), totalsLeft + 110f, y + 46f, whiteAmountPaint)
        y += 88f

        // Payment note + footer
        val noteRect = RectF(contentLeft, y, contentRight, y + 42f)
        fillPaint.color = lightGray2
        canvas.drawRoundRect(noteRect, 4f, 4f, fillPaint)
        canvas.drawRoundRect(noteRect, 4f, 4f, borderPaint)
        drawWrappedPlainText(
            canvas,
            "Please make the payment in 10 working days. Reference invoice number ${invoiceData.invoiceNumber} when making payment. Direct bank transfer to the account details listed above.",
            contentLeft + 12f,
            y + 16f,
            contentWidth - 24f,
            smallPaint.apply { color = textGray },
            11f
        )
        y += 54f

        canvas.drawLine(contentLeft, y, contentRight, y, subtleBorderPaint)
        y += 18f
        canvas.drawText("Thank you for choosing ${invoiceData.businessName} ${invoiceData.businessSubtitle}", contentLeft + 80f, y, bodyBoldPaint)
        y += 14f
        canvas.drawText("Document generated by ${invoiceData.businessName} invoicing system — Page 1 of 1", contentLeft + 70f, y, smallPaint)

        canvas.drawRect(contentLeft, pageHeight - margin - 3f, contentRight, pageHeight - margin, accentPaint)
    }

    private fun drawCardLabelValue(
        canvas: Canvas,
        x: Float,
        y: Float,
        label: String,
        value: String,
        labelPaint: Paint,
        valuePaint: Paint
    ) {
        canvas.drawText(label, x, y, labelPaint)
        canvas.drawText(value, x, y + 14f, valuePaint)
    }

    private fun drawTableHeaderText(canvas: Canvas, text: String, x: Float, y: Float, paint: Paint) {
        canvas.drawText(text, x, y, paint)
    }

    private fun drawAmountRow(
        canvas: Canvas,
        label: String,
        amount: Double,
        x: Float,
        y: Float,
        labelPaint: Paint,
        valuePaint: Paint
    ) {
        canvas.drawText(label, x, y, labelPaint)
        canvas.drawText(CurrencyFormatUtils.formatCurrency(amount), x + 150f, y, valuePaint)
    }

    private fun drawWrappedText(
        canvas: Canvas,
        label: String,
        text: String,
        x: Float,
        y: Float,
        maxWidth: Float,
        labelPaint: Paint,
        textPaint: Paint
    ) {
        canvas.drawText(label, x, y, labelPaint)
        drawWrappedPlainText(canvas, text, x, y + 12f, maxWidth, textPaint, 12f)
    }

    private fun drawWrappedPlainText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        maxWidth: Float,
        paint: Paint,
        lineHeight: Float
    ) {
        var currentY = y
        text.split("\n").forEach { paragraph ->
            val words = paragraph.split(" ")
            var line = ""
            words.forEach { word ->
                val candidate = if (line.isBlank()) word else "$line $word"
                if (paint.measureText(candidate) > maxWidth && line.isNotBlank()) {
                    canvas.drawText(line, x, currentY, paint)
                    currentY += lineHeight
                    line = word
                } else {
                    line = candidate
                }
            }
            if (line.isNotBlank()) {
                canvas.drawText(line, x, currentY, paint)
                currentY += lineHeight
            }
        }
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

    private fun drawLine(cursor: PageCursor, paint: Paint) {
        cursor.page.canvas.drawLine(margin, cursor.y, pageWidth - margin, cursor.y, paint)
    }

    private fun drawFooter(cursor: PageCursor, paint: Paint) {
        val footerY = pageHeight - margin
        val text = "Generated by ProPainters app - Page ${cursor.pageNumber}"
        cursor.page.canvas.drawText(text, margin, footerY, paint)
    }

}

