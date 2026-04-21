package com.example.propaintersplastererspayment.core.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.Path
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
        var totalPages: Int,
        var y: Float
    )

    private val pageWidth = 595
    private val pageHeight = 842
    private val margin = 40f

    // Colors
    private val colorDarkNavy = Color.parseColor("#2C3E50")
    private val colorOffWhite = Color.parseColor("#FDFCFB")
    private val colorGoldAccent = Color.parseColor("#9D8560")
    private val colorLightGray = Color.parseColor("#F1F5F9")
    private val colorMediumGray = Color.parseColor("#94A3B8")
    private val colorTextGray = Color.parseColor("#64748B")
    private val colorBorderGray = Color.parseColor("#CBD5E1")
    private val colorWhite = Color.WHITE

    fun exportTimesheetPdf(context: Context, data: TimesheetPdfData, outputFile: File): File {
        val document = PdfDocument()
        
        // Calculate total pages
        val totalPages = calculateTotalPages(data)
        
        var cursor = startDocument(document, 1, totalPages)

        // Draw First Page Header
        drawTimesheetHeader(cursor, data, isFirstPage = true)

        // Work Entries Table
        drawSectionTitleLuxury(cursor, "Work Entries")
        cursor.y += 12f
        
        drawWorkEntriesTable(cursor, data)

        // Materials Section (if space allows or on next page)
        if (data.materials.isNotEmpty()) {
            ensureSpaceLuxury(cursor, 100f, data)
            drawSectionTitleLuxury(cursor, "Materials")
            cursor.y += 12f
            drawMaterialsTable(cursor, data)
        }

        drawFooterLuxury(cursor)
        finishDocument(cursor)
        writeDocument(document, outputFile)
        return outputFile
    }

    private fun calculateTotalPages(data: TimesheetPdfData): Int {
        var pages = 1
        var currentY = margin + 130f // First page header space
        currentY += 40f // "Work Entries" title + space
        
        val rowHeight = 22f
        val headerHeight = 25f
        val totalRowHeight = 40f
        val bottomLimit = pageHeight - margin - 40f
        
        currentY += headerHeight
        
        var entriesOnPage = 0
        data.workEntries.forEachIndexed { index, _ ->
            if (entriesOnPage >= 20 || currentY + rowHeight > bottomLimit) {
                pages++
                currentY = margin + 55f + headerHeight // Continuation header + table header
                entriesOnPage = 0
            }
            currentY += rowHeight
            entriesOnPage++
        }
        
        // Total Hours row
        if (currentY + totalRowHeight > bottomLimit) {
            pages++
            currentY = margin + 55f
        }
        currentY += totalRowHeight
        
        // Materials Section
        if (data.materials.isNotEmpty()) {
            if (currentY + 100f > bottomLimit) {
                pages++
                currentY = margin + 55f
            }
            currentY += 40f + headerHeight // Title + Table header
            
            data.materials.forEach { _ ->
                if (currentY + rowHeight > bottomLimit) {
                    pages++
                    currentY = margin + 55f + headerHeight
                }
                currentY += rowHeight
            }
            currentY += 30f // Total materials row
        }
        
        return pages
    }

    private fun startDocument(document: PdfDocument, pageNumber: Int, totalPages: Int): PageCursor {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        val page = document.startPage(pageInfo)
        // Background
        page.canvas.drawColor(colorOffWhite)
        return PageCursor(document = document, page = page, pageNumber = pageNumber, totalPages = totalPages, y = margin)
    }

    private fun finishDocument(cursor: PageCursor) {
        cursor.document.finishPage(cursor.page)
    }

    private fun ensureSpaceLuxury(
        cursor: PageCursor,
        requiredHeight: Float,
        data: TimesheetPdfData
    ) {
        val bottomLimit = pageHeight - margin - 40f
        if (cursor.y + requiredHeight <= bottomLimit) return

        drawFooterLuxury(cursor)
        cursor.document.finishPage(cursor.page)
        cursor.pageNumber += 1
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, cursor.pageNumber).create()
        cursor.page = cursor.document.startPage(pageInfo)
        cursor.page.canvas.drawColor(colorOffWhite)
        cursor.y = margin
        
        // Draw continuation header
        drawTimesheetHeader(cursor, data, isFirstPage = false)
        cursor.y += 10f
    }

    private fun drawTimesheetHeader(cursor: PageCursor, data: TimesheetPdfData, isFirstPage: Boolean) {
        val canvas = cursor.page.canvas
        val yStart = cursor.y

        // 0. Top Decorative Bar (Dark Navy + Gold)
        val darkBarPaint = Paint().apply { color = Color.parseColor("#1E293B"); style = Paint.Style.FILL }
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 12f, darkBarPaint)
        val accentPaint = Paint().apply { color = colorGoldAccent; style = Paint.Style.FILL }
        canvas.drawRect(0f, 12f, pageWidth.toFloat(), 14f, accentPaint)

        if (isFirstPage) {
            // 1. LOGO BADGE (Left)
            val badgeSize = 46f
            val badgeRect = RectF(margin, yStart, margin + badgeSize, yStart + badgeSize)
            val darkFillPaint = Paint().apply { color = Color.parseColor("#2C3E50"); style = Paint.Style.FILL }
            canvas.drawRoundRect(badgeRect, 8f, 8f, darkFillPaint)
            
            val logoTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colorGoldAccent
                textSize = 16f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("PPP", badgeRect.centerX(), badgeRect.centerY() + 6f, logoTextPaint)

            // 2. BUSINESS NAME & DETAILS
            val busNamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#1E293B")
                textSize = 22f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val busSubNamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colorGoldAccent
                textSize = 16f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                letterSpacing = 0.15f
            }
            
            val detailX = margin + 58f
            var currentY = yStart + 17f
            canvas.drawText("Pro Painters", detailX, currentY, busNamePaint)
            currentY += 20f
            canvas.drawText("& PLASTERERS", detailX, currentY, busSubNamePaint)

            val detailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colorTextGray
                textSize = 8f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
            
            currentY += 22f // Increased from 15f to add gap below logo/name
            // Business Address - Moved back to left (margin) to align with logo
            drawWrappedPlainText(
                canvas,
                data.business.address,
                margin,
                currentY,
                210f,
                detailPaint,
                10f,
                maxLines = 2
            )
            
            currentY += 20f // Reduced from 24f to close gap with phone/email
            canvas.drawText("P: ${data.business.phoneNumber} | E: ${data.business.email}", margin, currentY, detailPaint)
            currentY += 10f // Reduced from 12f to close gap with account
            canvas.drawText("Account: ${data.business.bankAccountNumber}", margin, currentY, detailPaint)

            // 3. CENTER TITLE (TIMESHEET) - Moved up to align with Logo/Job Box top row
            drawTimesheetTitleWithOrnaments(canvas, 335f, yStart + 28f)

            // 4. JOB DETAILS BOX (Right)
            val boxWidth = 135f
            val boxHeight = 105f
            val boxRect = RectF(pageWidth - margin - boxWidth, yStart, pageWidth - margin, yStart + boxHeight)
            
            // Subtle off-white background with light border
            val boxBgPaint = Paint().apply { color = Color.parseColor("#FDFCFB"); style = Paint.Style.FILL }
            val boxBorderPaint = Paint().apply { 
                color = Color.parseColor("#F1F5F9")
                style = Paint.Style.STROKE
                strokeWidth = 1f 
            }
            canvas.drawRoundRect(boxRect, 10f, 10f, boxBgPaint)
            canvas.drawRoundRect(boxRect, 10f, 10f, boxBorderPaint)

            val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { 
                color = Color.parseColor("#94A3B8")
                textSize = 7f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                letterSpacing = 0.1f
            }
            val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { 
                color = Color.parseColor("#1E293B")
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) 
            }
            val dateValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { 
                color = colorGoldAccent
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) 
            }

            val bx = boxRect.left + 12f
            var by = boxRect.top + 20f
            
            canvas.drawText("NAME", bx, by, labelPaint)
            canvas.drawText(ellipsizeToWidth(data.jobName.ifBlank { "N/A" }, valuePaint, boxWidth - 24f), bx, by + 14f, valuePaint)
            
            by += 32f
            canvas.drawText("ADDRESS", bx, by, labelPaint)
            canvas.drawText(ellipsizeToWidth(data.jobAddress.ifBlank { "N/A" }, valuePaint, boxWidth - 24f), bx, by + 14f, valuePaint)

            by += 32f
            canvas.drawText("EXPORTED", bx, by, labelPaint)
            canvas.drawText(DateFormatUtils.formatDisplayDate(data.exportedAt), bx, by + 14f, dateValuePaint)

            cursor.y = yStart + 125f
        } else {
            // Continuation Header
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#1E293B")
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText("${data.business.businessName} - TIMESHEET (Continued)", margin, yStart + 15f, paint)
            
            val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colorTextGray
                textSize = 10f
            }
            canvas.drawText("Job: ${data.jobName} | Page ${cursor.pageNumber}", margin, yStart + 30f, subPaint)
            
            val linePaint = Paint().apply { color = colorGoldAccent; strokeWidth = 1f }
            canvas.drawLine(margin, yStart + 40f, pageWidth - margin, yStart + 40f, linePaint)
            
            cursor.y = yStart + 55f
        }
    }

    private fun drawTimesheetTitleWithOrnaments(canvas: Canvas, cx: Float, cy: Float) {
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E293B")
            textSize = 20f // Further reduced from 28f
            typeface = Typeface.create("serif", Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.2f
        }
        
        val text = "TIMESHEET"
        
        // Ornament lines - Top and Bottom
        val linePaint = Paint().apply { color = colorGoldAccent; strokeWidth = 0.6f }
        val ornamentWidth = 70f // Further reduced from 110f
        
        // Top ornament line
        canvas.drawLine(cx - ornamentWidth/2, cy - 20f, cx + ornamentWidth/2, cy - 20f, linePaint)
        
        // Diamond ornament top
        val diamondPath = Path().apply {
            moveTo(cx, cy - 24f)
            lineTo(cx + 3f, cy - 20f)
            lineTo(cx, cy - 16f)
            lineTo(cx - 3f, cy - 20f)
            close()
        }
        canvas.drawPath(diamondPath, Paint().apply { color = colorGoldAccent; style = Paint.Style.FILL })
        
        // Bottom ornament line (shorter)
        val bottomLineY = cy + 10f
        canvas.drawLine(cx - ornamentWidth/4, bottomLineY, cx + ornamentWidth/4, bottomLineY, linePaint)
        
        canvas.drawText(text, cx, cy + 4f, titlePaint)
    }

    private fun drawSectionTitleLuxury(cursor: PageCursor, title: String) {
        val canvas = cursor.page.canvas
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E293B")
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.1f
        }
        
        // Accent dot
        val dotPaint = Paint().apply { color = colorGoldAccent; style = Paint.Style.FILL }
        canvas.drawCircle(margin + 3f, cursor.y - 4f, 3f, dotPaint)
        
        canvas.drawText(title.uppercase(), margin + 12f, cursor.y, paint)
        
        // Line
        val linePaint = Paint().apply { color = colorGoldAccent; strokeWidth = 0.5f }
        val textWidth = paint.measureText(title.uppercase())
        canvas.drawLine(margin + 12f + textWidth + 10f, cursor.y - 4f, pageWidth - margin, cursor.y - 4f, linePaint)
    }

    private fun drawWorkEntriesTable(cursor: PageCursor, data: TimesheetPdfData) {
        val colDate = margin + 10f
        val colWorker = margin + 100f
        val colStart = margin + 250f
        val colFinish = margin + 320f
        val colHours = pageWidth - margin - 10f

        // Header
        drawWorkEntriesTableHeader(cursor, colDate, colWorker, colStart, colFinish, colHours)
        
        // Rows
        val rowHeight = 22f
        val normalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#475569"); textSize = 10f }
        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1E293B"); textSize = 10f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
        
        var entriesOnPage = 0
        data.workEntries.forEachIndexed { index, entry ->
            val needsNewPage = entriesOnPage >= 20
            
            if (needsNewPage) {
                goToNextPage(cursor, data)
                drawWorkEntriesTableHeader(cursor, colDate, colWorker, colStart, colFinish, colHours)
                entriesOnPage = 0
            } else {
                ensureSpaceWithHeader(cursor, rowHeight, data) {
                    drawWorkEntriesTableHeader(cursor, colDate, colWorker, colStart, colFinish, colHours)
                    entriesOnPage = 0
                }
            }
            
            if (index % 2 != 0) {
                val stripePaint = Paint().apply { color = colorLightGray; style = Paint.Style.FILL }
                cursor.page.canvas.drawRect(margin, cursor.y, pageWidth - margin, cursor.y + rowHeight, stripePaint)
            }
            
            val rowY = cursor.y + 15f
            cursor.page.canvas.drawText(DateFormatUtils.formatDisplayDate(entry.workDate), colDate, rowY, normalPaint)
            cursor.page.canvas.drawText(entry.workerName, colWorker, rowY, boldPaint)
            cursor.page.canvas.drawText(entry.startTime, colStart, rowY, normalPaint)
            cursor.page.canvas.drawText(entry.finishTime, colFinish, rowY, normalPaint)
            drawTextRight(cursor.page.canvas, WorkEntryTimeUtils.formatHours(entry.hoursWorked), colHours, rowY, boldPaint)
            
            cursor.y += rowHeight
            entriesOnPage++
        }
        
        // Total Hours Row
        val totalRowHeight = 30f
        ensureSpaceLuxury(cursor, totalRowHeight, data)
        val totalY = cursor.y + 20f
        val linePaint = Paint().apply { color = colorGoldAccent; strokeWidth = 1.5f }
        cursor.page.canvas.drawLine(pageWidth - margin - 150f, cursor.y, pageWidth - margin, cursor.y, linePaint)
        
        val totalLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1E293B"); textSize = 10f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
        drawTextRight(cursor.page.canvas, "TOTAL HOURS:", colHours - 60f, totalY, totalLabelPaint)
        drawTextRight(cursor.page.canvas, WorkEntryTimeUtils.formatHours(data.totalHours), colHours, totalY, totalLabelPaint)
        
        cursor.y += totalRowHeight + 10f
    }

    private fun drawWorkEntriesTableHeader(cursor: PageCursor, colDate: Float, colWorker: Float, colStart: Float, colFinish: Float, colHours: Float) {
        val canvas = cursor.page.canvas
        val headerHeight = 25f
        val headerRect = RectF(margin, cursor.y, pageWidth - margin, cursor.y + headerHeight)
        val headerPaint = Paint().apply { color = colorDarkNavy; style = Paint.Style.FILL }
        canvas.drawRect(headerRect, headerPaint)
        
        val headerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorWhite
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        
        val headerY = cursor.y + 16f
        canvas.drawText("DATE", colDate, headerY, headerTextPaint)
        canvas.drawText("WORKER", colWorker, headerY, headerTextPaint)
        canvas.drawText("START", colStart, headerY, headerTextPaint)
        canvas.drawText("FINISH", colFinish, headerY, headerTextPaint)
        drawTextRight(canvas, "HOURS", colHours, headerY, headerTextPaint)
        
        cursor.y += headerHeight
    }

    private fun goToNextPage(cursor: PageCursor, data: TimesheetPdfData) {
        drawFooterLuxury(cursor)
        cursor.document.finishPage(cursor.page)
        cursor.pageNumber += 1
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, cursor.pageNumber).create()
        cursor.page = cursor.document.startPage(pageInfo)
        cursor.page.canvas.drawColor(colorOffWhite)
        cursor.y = margin
        drawTimesheetHeader(cursor, data, isFirstPage = false)
        cursor.y += 10f
    }

    private fun ensureSpaceWithHeader(cursor: PageCursor, requiredHeight: Float, data: TimesheetPdfData, onNewPage: () -> Unit) {
        val bottomLimit = pageHeight - margin - 40f
        if (cursor.y + requiredHeight <= bottomLimit) return
        
        goToNextPage(cursor, data)
        onNewPage()
    }

    private fun drawMaterialsTable(cursor: PageCursor, data: TimesheetPdfData) {
        val canvas = cursor.page.canvas
        
        // Header
        val headerHeight = 25f
        val headerRect = RectF(margin, cursor.y, pageWidth - margin, cursor.y + headerHeight)
        val headerPaint = Paint().apply { color = colorDarkNavy; style = Paint.Style.FILL }
        canvas.drawRect(headerRect, headerPaint)
        
        val headerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorWhite
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        
        val colName = margin + 10f
        val colPrice = pageWidth - margin - 10f
        
        val headerY = cursor.y + 16f
        canvas.drawText("MATERIAL", colName, headerY, headerTextPaint)
        drawTextRight(canvas, "PRICE", colPrice, headerY, headerTextPaint)
        
        cursor.y += headerHeight
        
        val rowHeight = 22f
        val normalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#475569"); textSize = 10f }
        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1E293B"); textSize = 10f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }

        data.materials.forEachIndexed { index, material ->
            ensureSpaceLuxury(cursor, rowHeight, data)
            
            if (index % 2 != 0) {
                val stripePaint = Paint().apply { color = colorLightGray; style = Paint.Style.FILL }
                cursor.page.canvas.drawRect(margin, cursor.y, pageWidth - margin, cursor.y + rowHeight, stripePaint)
            }
            
            val rowY = cursor.y + 15f
            cursor.page.canvas.drawText(material.materialName, colName, rowY, boldPaint)
            drawTextRight(cursor.page.canvas, CurrencyFormatUtils.formatCurrency(material.price), colPrice, rowY, normalPaint)
            
            cursor.y += rowHeight
        }

        // Total Materials Row
        val totalRowHeight = 30f
        ensureSpaceLuxury(cursor, totalRowHeight, data)
        val totalY = cursor.y + 20f
        val linePaint = Paint().apply { color = colorGoldAccent; strokeWidth = 1.5f }
        cursor.page.canvas.drawLine(pageWidth - margin - 200f, cursor.y, pageWidth - margin, cursor.y, linePaint)
        
        val totalLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1E293B"); textSize = 10f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
        drawTextRight(cursor.page.canvas, "TOTAL MATERIAL COST:", colPrice - 80f, totalY, totalLabelPaint)
        drawTextRight(cursor.page.canvas, CurrencyFormatUtils.formatCurrency(data.totalMaterialCost), colPrice, totalY, totalLabelPaint)
        
        cursor.y += totalRowHeight
    }

    private fun drawFooterLuxury(cursor: PageCursor) {
        val canvas = cursor.page.canvas
        val footerY = pageHeight - margin
        
        // Bottom border accent
        val accentPaint = Paint().apply { color = Color.parseColor("#2C3E50"); style = Paint.Style.FILL }
        canvas.drawRect(margin, pageHeight.toFloat() - 3f, pageWidth - margin, pageHeight.toFloat(), accentPaint)

        val linePaint = Paint().apply { color = colorBorderGray; strokeWidth = 0.5f }
        canvas.drawLine(margin, footerY - 15f, pageWidth - margin, footerY - 15f, linePaint)
        
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorMediumGray
            textSize = 8f
            textAlign = Paint.Align.CENTER
        }
        val text = "Generated by Pro Painters Plasterers — Page ${cursor.pageNumber} of ${cursor.totalPages}"
        canvas.drawText(text, pageWidth / 2f, footerY, paint)
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
        val darkGray = Color.parseColor("#475569")
        val textGray = Color.parseColor("#64748B")
        val white = Color.WHITE

        val pageMargin = 28f
        val contentLeft = pageMargin
        val contentTop = pageMargin
        val contentRight = pageWidth - pageMargin
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
            color = Color.parseColor("#9D8560") // Luxury Gold
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val hugeTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = darkSlate
            textSize = 32f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val sectionLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#9D8560") // Luxury Gold
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

        // Header card with constrained title area so it cannot overlap business details.
        val headerHeight = 156f
        val headerRect = RectF(contentLeft, y, contentRight, y + headerHeight)
        canvas.drawRoundRect(headerRect, 4f, 4f, whiteFillPaint)
        canvas.drawRoundRect(headerRect, 4f, 4f, borderPaint)

        val headerPadding = 14f
        val titleAreaWidth = 138f
        val headerGap = 16f
        val leftSectionLeft = headerRect.left + headerPadding
        val leftSectionRight = headerRect.right - headerPadding - titleAreaWidth - headerGap
        val titleAreaLeft = leftSectionRight + headerGap
        val badgeRect = RectF(leftSectionLeft, y + 14f, leftSectionLeft + 40f, y + 54f)
        canvas.drawRoundRect(badgeRect, 3f, 3f, darkFillPaint)
        
        val logoTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bronze
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("PPP", badgeRect.centerX(), badgeRect.centerY() + 6f, logoTextPaint)

        val businessTextX = badgeRect.right + 12f
        val businessTextWidth = leftSectionRight - businessTextX
        val companyName = "Pro Painters Plasterers"
        canvas.drawText(
            ellipsizeToWidth(companyName, titlePaint, businessTextWidth),
            businessTextX,
            y + 42f,
            titlePaint
        )

        val leftInfoY = y + 74f
        val leftInfoWidth = leftSectionRight - leftSectionLeft
        val columnGap = 20f
        val addressWidth = leftInfoWidth * 0.45f
        val detailsWidth = leftInfoWidth - addressWidth - columnGap
        
        // Column 1: Address
        drawWrappedText(
            canvas,
            "ADDRESS:",
            invoiceData.businessAddress,
            leftSectionLeft,
            leftInfoY,
            addressWidth,
            sectionLabelPaint,
            bodyPaint,
            maxLines = 4
        )

        // Column 2: Contact Details (Rows for Contact, Email, Account)
        var detailRowY = leftInfoY
        
        // Row 1: Contact
        drawWrappedText(
            canvas,
            "CONTACT:",
            invoiceData.businessPhone,
            leftSectionLeft + addressWidth + columnGap,
            detailRowY,
            detailsWidth,
            sectionLabelPaint,
            bodyPaint,
            maxLines = 1
        )
        
        detailRowY += 28f
        
        // Row 2: Email
        drawWrappedText(
            canvas,
            "EMAIL:",
            invoiceData.businessEmail,
            leftSectionLeft + addressWidth + columnGap,
            detailRowY,
            detailsWidth,
            sectionLabelPaint,
            bodyPaint,
            maxLines = 1
        )
        
        detailRowY += 28f
        
        // Row 3: Account
        drawWrappedText(
            canvas,
            "ACCOUNT:",
            invoiceData.accountNumber,
            leftSectionLeft + addressWidth + columnGap,
            detailRowY,
            detailsWidth,
            sectionLabelPaint,
            bodyPaint,
            maxLines = 1
        )

        // Right title block is width-constrained and right-aligned to prevent overlap.
        canvas.drawLine(titleAreaLeft + 62f, y + 22f, headerRect.right - headerPadding, y + 22f, accentPaint)
        drawTextRight(
            canvas,
            "INVOICE",
            headerRect.right - headerPadding,
            y + 50f,
            hugeTitlePaint
        )
        y += headerHeight + 14f

        // Detail cards
        val cardGap = 12f
        val cardWidth = (contentWidth - cardGap) / 2f
        val leftCardHeight = 84f
        val rightCardHeight = 96f
        val cardsHeight = max(leftCardHeight, rightCardHeight)
        val leftCard = RectF(contentLeft, y, contentLeft + cardWidth, y + cardsHeight)
        val rightCard = RectF(contentLeft + cardWidth + cardGap, y, contentRight, y + cardsHeight)
        canvas.drawRoundRect(leftCard, 4f, 4f, whiteFillPaint)
        canvas.drawRoundRect(rightCard, 4f, 4f, whiteFillPaint)
        canvas.drawRoundRect(leftCard, 4f, 4f, subtleBorderPaint)
        canvas.drawRoundRect(rightCard, 4f, 4f, subtleBorderPaint)

        drawCardLabelValue(canvas, leftCard.left + 12f, leftCard.top + 20f, "INVOICE NUMBER", invoiceData.invoiceNumber, sectionLabelPaint, bodyBoldPaint)
        drawCardLabelValue(canvas, leftCard.left + 12f, leftCard.top + 48f, "ISSUE DATE", invoiceData.issueDate, sectionLabelPaint, bodyPaint)

        drawCardLabelValue(canvas, rightCard.left + 12f, rightCard.top + 20f, "BILL TO", invoiceData.billTo, sectionLabelPaint, bodyBoldPaint)
        drawWrappedText(
            canvas,
            "ADDRESS",
            invoiceData.jobAddress,
            rightCard.left + 12f,
            rightCard.top + 48f,
            cardWidth - 24f,
            sectionLabelPaint,
            bodyPaint,
            maxLines = 3
        )
        y += cardsHeight + 14f

        // Services header
        canvas.drawText("SERVICES & MATERIALS", contentLeft + 20f, y, bodyBoldPaint)
        y += 12f

        val tableHeaderHeight = 24f
        canvas.drawRect(contentLeft, y, contentRight, y + tableHeaderHeight, darkFillPaint2)
        val tablePadding = 12f
        val amountColWidth = 88f
        val rateColWidth = 84f
        val qtyColWidth = 44f
        val tableGap = 10f
        val amountRight = contentRight - tablePadding
        val amountLeft = amountRight - amountColWidth
        val rateRight = amountLeft - tableGap
        val rateLeft = rateRight - rateColWidth
        val qtyRight = rateLeft - tableGap
        val qtyLeft = qtyRight - qtyColWidth
        val descriptionLeft = contentLeft + tablePadding
        val descriptionRight = qtyLeft - tableGap
        val descriptionWidth = descriptionRight - descriptionLeft
        drawTableHeaderText(canvas, "DESCRIPTION", descriptionLeft, y + 16f, whiteTextPaint)
        drawTextCenter(canvas, "QTY", qtyLeft, qtyRight, y + 16f, whiteTextPaint)
        drawTextRight(canvas, "UNIT PRICE", rateRight, y + 16f, whiteTextPaint)
        drawTextRight(canvas, "LINE TOTAL", amountRight, y + 16f, whiteTextPaint)
        y += tableHeaderHeight

        invoiceData.lineItems.forEachIndexed { index, item ->
            val descriptionLines = wrapTextLines(item.description, bodyPaint, descriptionWidth, maxLines = 3)
            val rowHeight = max(28f, (descriptionLines.size * 12f) + 10f)
            fillPaint.color = if (index % 2 == 0) white else lightGray1
            canvas.drawRect(contentLeft, y, contentRight, y + rowHeight, fillPaint)
            canvas.drawLine(contentLeft, y + rowHeight, contentRight, y + rowHeight, subtleBorderPaint)
            var lineY = y + 16f
            descriptionLines.forEach { line ->
                canvas.drawText(line, descriptionLeft, lineY, bodyPaint)
                lineY += 12f
            }
            val numberBaseline = y + minOf(rowHeight - 8f, 18f)
            drawTextCenter(canvas, item.quantity.toString(), qtyLeft, qtyRight, numberBaseline, bodyPaint)
            drawTextRight(canvas, CurrencyFormatUtils.formatCurrency(item.rate), rateRight, numberBaseline, bodyPaint)
            drawTextRight(canvas, CurrencyFormatUtils.formatCurrency(item.amount), amountRight, numberBaseline, bodyBoldPaint)
            y += rowHeight
        }

        y += 14f

        // Totals cards
        val totalsWidth = minOf(236f, contentWidth)
        val totalsLeft = contentRight - totalsWidth
        val totalsCard = RectF(totalsLeft, y, contentRight, y + 64f)
        canvas.drawRoundRect(totalsCard, 4f, 4f, whiteFillPaint)
        canvas.drawRoundRect(totalsCard, 4f, 4f, subtleBorderPaint)
        drawAmountRow(canvas, "SUBTOTAL", invoiceData.subtotal, totalsLeft + 12f, contentRight - 12f, y + 18f, sectionLabelPaint, bodyPaint)
        if (invoiceData.includeGst) {
            drawAmountRow(canvas, "GST", invoiceData.gstAmount, totalsLeft + 12f, contentRight - 12f, y + 40f, sectionLabelPaint, bodyPaint)
        } else {
            drawAmountRow(canvas, "GST (EXEMPT)", 0.0, totalsLeft + 12f, contentRight - 12f, y + 40f, sectionLabelPaint, bodyPaint)
        }
        y += 74f

        val total = invoiceData.total
        val dueCard = RectF(totalsLeft, y, contentRight, y + 74f)
        canvas.drawRoundRect(dueCard, 4f, 4f, darkFillPaint)
        canvas.drawRoundRect(dueCard, 4f, 4f, borderPaint)
        canvas.drawText("AMOUNT DUE", totalsLeft + 12f, y + 24f, bronzeTextPaint)
        if (invoiceData.dueDate != null) {
            canvas.drawText("Due: ${invoiceData.dueDate}", totalsLeft + 12f, y + 40f, smallPaint.apply { color = Color.parseColor("#CBD5E1") })
        }
        drawTextRight(canvas, CurrencyFormatUtils.formatCurrency(total), contentRight - 12f, y + 46f, whiteAmountPaint)
        y += 88f

        // Payment note + footer
        if (invoiceData.dueDate != null) {
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
                11f,
                maxLines = 3
            )
            y += 54f
        }

        canvas.drawLine(contentLeft, y, contentRight, y, subtleBorderPaint)
        y += 18f
        drawTextCenter(
            canvas,
            "Thank you for choosing ${invoiceData.businessName} ${invoiceData.businessSubtitle}",
            contentLeft,
            contentRight,
            y,
            bodyBoldPaint
        )
        y += 14f
        drawTextCenter(
            canvas,
            "Document generated by ${invoiceData.businessName} invoicing system — Page 1 of 1",
            contentLeft,
            contentRight,
            y,
            smallPaint
        )

        canvas.drawRect(contentLeft, pageHeight - pageMargin - 3f, contentRight, pageHeight - pageMargin, accentPaint)
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
        rightX: Float,
        y: Float,
        labelPaint: Paint,
        valuePaint: Paint
    ) {
        canvas.drawText(label, x, y, labelPaint)
        drawTextRight(canvas, CurrencyFormatUtils.formatCurrency(amount), rightX, y, valuePaint)
    }

    private fun drawWrappedText(
        canvas: Canvas,
        label: String,
        text: String,
        x: Float,
        y: Float,
        maxWidth: Float,
        labelPaint: Paint,
        textPaint: Paint,
        maxLines: Int = Int.MAX_VALUE
    ) {
        canvas.drawText(label, x, y, labelPaint)
        drawWrappedPlainText(canvas, text, x, y + 12f, maxWidth, textPaint, 12f, maxLines)
    }

    private fun drawWrappedPlainText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        maxWidth: Float,
        paint: Paint,
        lineHeight: Float,
        maxLines: Int = Int.MAX_VALUE
    ) {
        val lines = wrapTextLines(text, paint, maxWidth, maxLines)
        var currentY = y
        lines.forEach { line ->
            canvas.drawText(line, x, currentY, paint)
            currentY += lineHeight
        }
    }

    private fun wrapTextLines(
        text: String,
        paint: Paint,
        maxWidth: Float,
        maxLines: Int = Int.MAX_VALUE
    ): List<String> {
        if (text.isBlank()) return emptyList()
        val result = mutableListOf<String>()
        text.split("\n").forEach { paragraph ->
            val words = paragraph.split(" ").filter { it.isNotBlank() }
            var line = ""
            for (word in words) {
                val candidate = if (line.isBlank()) word else "$line $word"
                if (paint.measureText(candidate) > maxWidth && line.isNotBlank()) {
                    result += line
                    line = word
                    if (result.size == maxLines) return result.dropLast(1) + ellipsizeToWidth(result.last(), paint, maxWidth)
                } else {
                    line = candidate
                }
            }
            if (line.isNotBlank()) {
                result += line
                if (result.size == maxLines) return result
            }
        }
        return if (result.size <= maxLines) result else result.take(maxLines)
    }

    private fun ellipsizeToWidth(text: String, paint: Paint, maxWidth: Float): String {
        if (paint.measureText(text) <= maxWidth) return text
        val ellipsis = "…"
        var trimmed = text
        while (trimmed.isNotEmpty() && paint.measureText(trimmed + ellipsis) > maxWidth) {
            trimmed = trimmed.dropLast(1)
        }
        return if (trimmed.isEmpty()) ellipsis else trimmed + ellipsis
    }

    private fun drawTextRight(canvas: Canvas, text: String, rightX: Float, baselineY: Float, paint: Paint) {
        canvas.drawText(text, rightX - paint.measureText(text), baselineY, paint)
    }

    private fun drawTextCenter(canvas: Canvas, text: String, leftX: Float, rightX: Float, baselineY: Float, paint: Paint) {
        val textWidth = paint.measureText(text)
        val x = leftX + ((rightX - leftX - textWidth) / 2f)
        canvas.drawText(ellipsizeToWidth(text, paint, rightX - leftX), x.coerceAtLeast(leftX), baselineY, paint)
    }

    private fun writeDocument(document: PdfDocument, outputFile: File) {
        outputFile.parentFile?.mkdirs()
        FileOutputStream(outputFile).use { stream ->
            document.writeTo(stream)
        }
        document.close()
    }

}
