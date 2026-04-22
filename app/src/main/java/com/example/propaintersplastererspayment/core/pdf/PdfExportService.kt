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

class PdfExportService {

    private data class PageCursor(
        var document: PdfDocument,
        var page: PdfDocument.Page,
        var pageNumber: Int,
        var totalPages: Int,
        var y: Float
    )

    private data class InvoicePageCursor(
        var document: PdfDocument,
        var page: PdfDocument.Page,
        var pageNumber: Int,
        var totalPages: Int,
        var y: Float
    )

    private val pageWidth = 595
    private val pageHeight = 842
    private val margin = 40f

    // Luxury Layout Constants
    private val luxColDesc = margin + 12f
    private val luxColQty = margin + 310f
    private val luxColRate = margin + 410f
    private val luxColAmount = pageWidth - margin - 12f
    private val luxTitleSize = 38f
    private val luxTitleLS = 0.4f
    private val luxTitleGap = 24f

    // Invoice layout tuning
    private val invoiceFirstPageItemsStartY = 306f
    private val invoiceContinuationItemsStartY = margin + 84f
    private val invoiceItemsSectionGap = 14f
    private val invoiceTableHeaderHeight = 32f
    private val invoiceRowHeight = 28f
    private val invoiceTotalsHeightWithGst = 86f
    private val invoiceTotalsHeightNoGst = 66f
    private val invoicePaymentHeight = 92f

    // Colors
    private val colorDarkNavy = Color.parseColor("#2C3E50")
    private val colorOffWhite = Color.parseColor("#FDFCFB")
    private val colorGoldAccent = Color.parseColor("#9D8560")
    private val colorIndustrialGold = Color.parseColor("#C06014")
    private val colorLightGray = Color.parseColor("#F1F5F9")
    private val colorMediumGray = Color.parseColor("#94A3B8")
    private val colorTextGray = Color.parseColor("#64748B")
    private val colorBorderGray = Color.parseColor("#CBD5E1")
    private val colorWhite = Color.WHITE

    fun exportTimesheetPdf(context: Context, data: TimesheetPdfData, outputFile: File): File {
        val document = PdfDocument()
        val renderer = TimesheetPdfRenderer(data, pageWidth, pageHeight, margin)

        val totalPages = calculateTotalPages(data)
        var cursor = startDocument(document, 1, totalPages)

        cursor.y = renderer.drawHeader(cursor.page.canvas, cursor.y, isFirstPage = true)

        renderer.drawSectionTitle(cursor.page.canvas, cursor.y, "Work Entries")
        cursor.y += TimesheetPdfRenderer.SECTION_TITLE_HEIGHT - 10f

        renderer.drawWorkEntriesTableHeader(cursor.page.canvas, cursor.y)
        cursor.y += TimesheetPdfRenderer.TABLE_HEADER_HEIGHT

        data.workEntries.forEachIndexed { index, entry ->
            ensureSpaceNew(cursor, TimesheetPdfRenderer.TABLE_ROW_HEIGHT, data, renderer) {
                renderer.drawWorkEntriesTableHeader(cursor.page.canvas, cursor.y)
                cursor.y += TimesheetPdfRenderer.TABLE_HEADER_HEIGHT
            }
            renderer.drawWorkEntryRow(cursor.page.canvas, cursor.y, entry, index % 2 != 0)
            cursor.y += TimesheetPdfRenderer.TABLE_ROW_HEIGHT
        }

        ensureSpaceNew(cursor, TimesheetPdfRenderer.TABLE_TOTAL_HEIGHT, data, renderer)
        renderer.drawTotalHours(cursor.page.canvas, cursor.y, data.totalHours)
        cursor.y += TimesheetPdfRenderer.TABLE_TOTAL_HEIGHT + 20f

        if (data.materials.isNotEmpty()) {
            ensureSpaceNew(
                cursor,
                TimesheetPdfRenderer.SECTION_TITLE_HEIGHT + TimesheetPdfRenderer.TABLE_HEADER_HEIGHT + TimesheetPdfRenderer.TABLE_ROW_HEIGHT,
                data,
                renderer
            )

            renderer.drawSectionTitle(cursor.page.canvas, cursor.y, "Materials")
            cursor.y += TimesheetPdfRenderer.SECTION_TITLE_HEIGHT - 10f

            renderer.drawMaterialsTableHeader(cursor.page.canvas, cursor.y)
            cursor.y += TimesheetPdfRenderer.TABLE_HEADER_HEIGHT

            data.materials.forEachIndexed { index, material ->
                ensureSpaceNew(cursor, TimesheetPdfRenderer.TABLE_ROW_HEIGHT, data, renderer) {
                    renderer.drawMaterialsTableHeader(cursor.page.canvas, cursor.y)
                    cursor.y += TimesheetPdfRenderer.TABLE_HEADER_HEIGHT
                }
                renderer.drawMaterialRow(cursor.page.canvas, cursor.y, material, index % 2 != 0)
                cursor.y += TimesheetPdfRenderer.TABLE_ROW_HEIGHT
            }

            ensureSpaceNew(cursor, TimesheetPdfRenderer.TABLE_TOTAL_HEIGHT, data, renderer)
            renderer.drawTotalMaterialCost(cursor.page.canvas, cursor.y, data.totalMaterialCost)
        }

        renderer.drawFooter(cursor.page.canvas, cursor.pageNumber, cursor.totalPages)
        finishDocument(cursor)
        writeDocument(document, outputFile)
        return outputFile
    }

    private fun calculateTotalPages(data: TimesheetPdfData): Int {
        var pages = 1
        var currentY = margin + TimesheetPdfRenderer.HEADER_HEIGHT_FIRST
        currentY += TimesheetPdfRenderer.SECTION_TITLE_HEIGHT

        val rowHeight = TimesheetPdfRenderer.TABLE_ROW_HEIGHT
        val headerHeight = TimesheetPdfRenderer.TABLE_HEADER_HEIGHT
        val totalRowHeight = TimesheetPdfRenderer.TABLE_TOTAL_HEIGHT
        val bottomLimit = pageHeight - margin - 40f

        currentY += headerHeight

        data.workEntries.forEach {
            if (currentY + rowHeight > bottomLimit) {
                pages++
                currentY = margin + TimesheetPdfRenderer.HEADER_HEIGHT_CONT + headerHeight
            }
            currentY += rowHeight
        }

        if (currentY + totalRowHeight > bottomLimit) {
            pages++
            currentY = margin + TimesheetPdfRenderer.HEADER_HEIGHT_CONT
        }
        currentY += totalRowHeight + 20f

        if (data.materials.isNotEmpty()) {
            if (currentY + TimesheetPdfRenderer.SECTION_TITLE_HEIGHT + headerHeight + rowHeight > bottomLimit) {
                pages++
                currentY = margin + TimesheetPdfRenderer.HEADER_HEIGHT_CONT
            }
            currentY += TimesheetPdfRenderer.SECTION_TITLE_HEIGHT + headerHeight

            data.materials.forEach {
                if (currentY + rowHeight > bottomLimit) {
                    pages++
                    currentY = margin + TimesheetPdfRenderer.HEADER_HEIGHT_CONT + headerHeight
                }
                currentY += rowHeight
            }

            if (currentY + totalRowHeight > bottomLimit) {
                pages++
                currentY = margin + TimesheetPdfRenderer.HEADER_HEIGHT_CONT
            }
            currentY += totalRowHeight
        }

        return pages
    }

    private fun ensureSpaceNew(
        cursor: PageCursor,
        requiredHeight: Float,
        data: TimesheetPdfData,
        renderer: TimesheetPdfRenderer,
        onNewPage: (() -> Unit)? = null
    ) {
        val bottomLimit = pageHeight - margin - 40f
        if (cursor.y + requiredHeight <= bottomLimit) return

        renderer.drawFooter(cursor.page.canvas, cursor.pageNumber, cursor.totalPages)
        cursor.document.finishPage(cursor.page)
        cursor.pageNumber += 1
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, cursor.pageNumber).create()
        cursor.page = cursor.document.startPage(pageInfo)
        cursor.page.canvas.drawColor(colorWhite)
        cursor.y = margin

        cursor.y = renderer.drawHeader(cursor.page.canvas, cursor.y, isFirstPage = false)
        onNewPage?.invoke()
    }

    private fun startDocument(document: PdfDocument, pageNumber: Int, totalPages: Int): PageCursor {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        val page = document.startPage(pageInfo)
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

        drawTimesheetHeader(cursor, data, isFirstPage = false)
        cursor.y += 10f
    }

    private fun drawTimesheetHeader(cursor: PageCursor, data: TimesheetPdfData, isFirstPage: Boolean) {
        val canvas = cursor.page.canvas
        val yStart = cursor.y

        val darkBarPaint = Paint().apply { color = Color.parseColor("#1E293B"); style = Paint.Style.FILL }
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 12f, darkBarPaint)
        val accentPaint = Paint().apply { color = colorGoldAccent; style = Paint.Style.FILL }
        canvas.drawRect(0f, 12f, pageWidth.toFloat(), 14f, accentPaint)

        if (isFirstPage) {
            val leftWidth = 220f
            val rightWidth = 160f

            val badgeSize = 56f
            val badgeTop = yStart + 6f
            val badgeRect = RectF(margin, badgeTop, margin + badgeSize, badgeTop + badgeSize)
            val darkFillPaint = Paint().apply { color = Color.parseColor("#2C3E50"); style = Paint.Style.FILL }
            canvas.drawRoundRect(badgeRect, 8f, 8f, darkFillPaint)

            val logoTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colorGoldAccent
                textSize = 18f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("PPP", badgeRect.centerX(), badgeRect.centerY() + 7f, logoTextPaint)

            val companyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#1E293B")
                textSize = 20f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val companySubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colorGoldAccent
                textSize = 13f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }

            val leftTextX = badgeRect.right + 12f
            var leftY = badgeTop + 18f
            canvas.drawText("Pro Painters", leftTextX, leftY, companyPaint)
            leftY += 20f
            canvas.drawText("& PLASTERERS", leftTextX, leftY, companySubPaint)

            val detailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colorTextGray
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }

            leftY += 18f
            val addrMaxWidth = leftWidth - (badgeSize + 24f)
            drawWrappedPlainText(canvas, data.business.address, leftTextX, leftY, addrMaxWidth, detailPaint, 11f, maxLines = 2)
            leftY += 26f

            canvas.drawText("P: ${data.business.phoneNumber}", leftTextX, leftY, detailPaint)
            leftY += 14f

            canvas.drawText("E: ${data.business.email}", leftTextX, leftY, detailPaint)

            val centerX = pageWidth / 2f
            val headerCenterY = yStart + (badgeSize / 2f) + 12f
            drawTimesheetTitleWithOrnaments(canvas, centerX, headerCenterY)

            val boxLeft = pageWidth - margin - rightWidth
            val labelX = boxLeft + 8f
            val valueX = labelX
            var by = yStart + 12f

            val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colorMediumGray
                textSize = 8f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }

            val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#1E293B")
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val dateValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colorGoldAccent
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            canvas.drawText("NAME", labelX, by, labelPaint)
            canvas.drawText(ellipsizeToWidth(data.jobName.ifBlank { "N/A" }, valuePaint, rightWidth - 20f), valueX, by + 16f, valuePaint)

            by += 44f
            canvas.drawText("ADDRESS", labelX, by, labelPaint)
            drawWrappedPlainText(canvas, data.jobAddress.ifBlank { "N/A" }, valueX, by + 16f, rightWidth - 20f, valuePaint, 14f, maxLines = 2)

            by += 52f
            canvas.drawText("ISSUE DATE", labelX, by, labelPaint)
            canvas.drawText(DateFormatUtils.formatDisplayDate(data.exportedAt), valueX, by + 16f, dateValuePaint)

            cursor.y = yStart + maxOf(badgeSize + 24f, 140f)
        } else {
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
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.35f
        }

        val text = "TIMESHEET"

        val linePaint = Paint().apply { color = colorGoldAccent; strokeWidth = 0.8f }
        val ornamentWidth = 200f

        val topY = cy - 28f
        canvas.drawLine(cx - ornamentWidth / 2f, topY, cx + ornamentWidth / 2f, topY, linePaint)

        val diamondSize = 5f
        val leftDiamondPath = Path().apply {
            val dx = cx - ornamentWidth / 2f
            moveTo(dx, topY)
            lineTo(dx + diamondSize, topY + diamondSize)
            lineTo(dx, topY + (diamondSize * 2f))
            lineTo(dx - diamondSize, topY + diamondSize)
            close()
        }
        canvas.drawPath(leftDiamondPath, Paint().apply { color = colorGoldAccent; style = Paint.Style.FILL })

        val rightDx = cx + ornamentWidth / 2f
        val rightDiamondPath = Path().apply {
            moveTo(rightDx, topY)
            lineTo(rightDx + diamondSize, topY + diamondSize)
            lineTo(rightDx, topY + (diamondSize * 2f))
            lineTo(rightDx - diamondSize, topY + diamondSize)
            close()
        }
        canvas.drawPath(rightDiamondPath, Paint().apply { color = colorGoldAccent; style = Paint.Style.FILL })

        val bottomY = cy + 30f
        canvas.drawLine(cx - ornamentWidth / 2f, bottomY, cx + ornamentWidth / 2f, bottomY, linePaint)

        val leftDiamondBottom = Path().apply {
            val dx = cx - ornamentWidth / 2f
            moveTo(dx, bottomY)
            lineTo(dx + diamondSize, bottomY - diamondSize)
            lineTo(dx, bottomY - (diamondSize * 2f))
            lineTo(dx - diamondSize, bottomY - diamondSize)
            close()
        }
        canvas.drawPath(leftDiamondBottom, Paint().apply { color = colorGoldAccent; style = Paint.Style.FILL })

        val rightDiamondBottom = Path().apply {
            val dx = cx + ornamentWidth / 2f
            moveTo(dx, bottomY)
            lineTo(dx + diamondSize, bottomY - diamondSize)
            lineTo(dx, bottomY - (diamondSize * 2f))
            lineTo(dx - diamondSize, bottomY - diamondSize)
            close()
        }
        canvas.drawPath(rightDiamondBottom, Paint().apply { color = colorGoldAccent; style = Paint.Style.FILL })

        canvas.drawText(text, cx, cy + 6f, titlePaint)
    }

    private fun drawSectionTitleLuxury(cursor: PageCursor, title: String) {
        val canvas = cursor.page.canvas
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E293B")
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.1f
        }

        val dotPaint = Paint().apply { color = colorGoldAccent; style = Paint.Style.FILL }
        canvas.drawCircle(margin + 3f, cursor.y - 4f, 3f, dotPaint)

        canvas.drawText(title.uppercase(), margin + 12f, cursor.y, paint)

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

        drawWorkEntriesTableHeader(cursor, colDate, colWorker, colStart, colFinish, colHours)

        val rowHeight = 22f
        val normalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#475569"); textSize = 10f }
        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E293B")
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

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

        val totalRowHeight = 30f
        ensureSpaceLuxury(cursor, totalRowHeight, data)
        val totalY = cursor.y + 20f
        val linePaint = Paint().apply { color = colorGoldAccent; strokeWidth = 1.5f }
        cursor.page.canvas.drawLine(pageWidth - margin - 150f, cursor.y, pageWidth - margin, cursor.y, linePaint)

        val totalLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E293B")
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
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
        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E293B")
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

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

        val totalRowHeight = 30f
        ensureSpaceLuxury(cursor, totalRowHeight, data)
        val totalY = cursor.y + 20f
        val linePaint = Paint().apply { color = colorGoldAccent; strokeWidth = 1.5f }
        cursor.page.canvas.drawLine(pageWidth - margin - 200f, cursor.y, pageWidth - margin, cursor.y, linePaint)

        val totalLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E293B")
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        drawTextRight(cursor.page.canvas, "TOTAL MATERIAL COST:", colPrice - 80f, totalY, totalLabelPaint)
        drawTextRight(cursor.page.canvas, CurrencyFormatUtils.formatCurrency(data.totalMaterialCost), colPrice, totalY, totalLabelPaint)

        cursor.y += totalRowHeight
    }

    private fun drawFooterLuxury(cursor: PageCursor) {
        val canvas = cursor.page.canvas
        val footerY = pageHeight - margin

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

        val totalPages = calculateInvoiceTotalPages(invoiceData)
        val cursor = startInvoiceDocument(document, 1, totalPages)

        renderInvoiceDocument(cursor, invoiceData)

        cursor.document.finishPage(cursor.page)
        writeDocument(document, outputFile)
        return outputFile
    }

    private fun startInvoiceDocument(
        document: PdfDocument,
        pageNumber: Int,
        totalPages: Int
    ): InvoicePageCursor {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        val page = document.startPage(pageInfo)
        page.canvas.drawColor(colorWhite)
        return InvoicePageCursor(
            document = document,
            page = page,
            pageNumber = pageNumber,
            totalPages = totalPages,
            y = margin
        )
    }

    private fun calculateInvoiceTotalPages(invoiceData: InvoiceData): Int {
        val bottomLimit = pageHeight - margin - 50f
        val totalsHeight = if (invoiceData.includeGst) invoiceTotalsHeightWithGst else invoiceTotalsHeightNoGst

        var pages = 1
        var currentY = invoiceFirstPageItemsStartY

        currentY += invoiceItemsSectionGap + invoiceTableHeaderHeight

        invoiceData.lineItems.forEach {
            if (currentY + invoiceRowHeight > bottomLimit) {
                pages++
                currentY = invoiceContinuationItemsStartY + invoiceItemsSectionGap + invoiceTableHeaderHeight
            }
            currentY += invoiceRowHeight
        }

        if (currentY + 16f + totalsHeight > bottomLimit) {
            pages++
            currentY = invoiceContinuationItemsStartY
        }
        currentY += 16f + totalsHeight + 14f

        if (currentY + invoicePaymentHeight > bottomLimit) {
            pages++
        }

        return pages
    }

    private fun renderInvoiceDocument(
        cursor: InvoicePageCursor,
        invoiceData: InvoiceData
    ) {
        cursor.y = drawInvoiceFirstPageHeader(cursor.page.canvas, invoiceData)

        drawInvoiceItemsSectionHeader(cursor.page.canvas, cursor.y)
        cursor.y += invoiceItemsSectionGap

        drawInvoiceItemsTableHeader(cursor.page.canvas, cursor.y)
        cursor.y += invoiceTableHeaderHeight

        invoiceData.lineItems.forEachIndexed { index, item ->
            ensureInvoiceSpace(cursor, invoiceRowHeight) {
                drawInvoiceContinuationHeader(cursor.page.canvas, invoiceData, cursor.pageNumber, cursor.totalPages)
                cursor.y = invoiceContinuationItemsStartY
                drawInvoiceItemsSectionHeader(cursor.page.canvas, cursor.y)
                cursor.y += invoiceItemsSectionGap
                drawInvoiceItemsTableHeader(cursor.page.canvas, cursor.y)
                cursor.y += invoiceTableHeaderHeight
            }

            drawInvoiceItemRow(
                canvas = cursor.page.canvas,
                y = cursor.y,
                description = item.description,
                quantity = item.quantity,
                isLabour = item.isLabour,
                rate = item.rate,
                amount = item.amount,
                striped = index % 2 != 0
            )
            cursor.y += invoiceRowHeight
        }

        cursor.y += 16f

        val totalsRequiredHeight = if (invoiceData.includeGst) invoiceTotalsHeightWithGst else invoiceTotalsHeightNoGst
        ensureInvoiceSpace(cursor, totalsRequiredHeight + 8f) {
            drawInvoiceContinuationHeader(cursor.page.canvas, invoiceData, cursor.pageNumber, cursor.totalPages)
            cursor.y = invoiceContinuationItemsStartY
        }

        drawInvoiceTotals(cursor.page.canvas, cursor.y, invoiceData)
        cursor.y += totalsRequiredHeight + 14f

        ensureInvoiceSpace(cursor, invoicePaymentHeight) {
            drawInvoiceContinuationHeader(cursor.page.canvas, invoiceData, cursor.pageNumber, cursor.totalPages)
            cursor.y = invoiceContinuationItemsStartY
        }

        drawInvoicePaymentInformation(cursor.page.canvas, cursor.y, invoiceData)
        drawInvoiceFooter(cursor.page.canvas, cursor.pageNumber, cursor.totalPages)
    }

    private fun ensureInvoiceSpace(
        cursor: InvoicePageCursor,
        requiredHeight: Float,
        onNewPage: (() -> Unit)? = null
    ) {
        val bottomLimit = pageHeight - margin - 50f
        if (cursor.y + requiredHeight <= bottomLimit) return

        drawInvoiceFooter(cursor.page.canvas, cursor.pageNumber, cursor.totalPages)
        cursor.document.finishPage(cursor.page)

        cursor.pageNumber += 1
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, cursor.pageNumber).create()
        cursor.page = cursor.document.startPage(pageInfo)
        cursor.page.canvas.drawColor(colorWhite)

        onNewPage?.invoke()
    }

    private fun drawInvoiceFirstPageHeader(canvas: Canvas, invoiceData: InvoiceData): Float {
        val colorNavy = Color.parseColor("#1E293B")
        val colorNavyLight = Color.parseColor("#334155")
        val colorBronze = Color.parseColor("#9D8560")
        val colorLightGray1 = Color.parseColor("#F7F5F2")
        val localTextGray = Color.parseColor("#64748B")
        val localMediumGray = Color.parseColor("#94A3B8")
        val localBorderGray = Color.parseColor("#CBD5E1")

        var y = margin
        val contentWidth = pageWidth - 2 * margin

        val topBorderPaint = Paint().apply { color = colorNavy; style = Paint.Style.FILL }
        canvas.drawRect(margin, y, pageWidth - margin, y + 4f, topBorderPaint)
        y += 28f

        val badgeSize = 60f
        val badgeRect = RectF(margin, y, margin + badgeSize, y + badgeSize)
        val logoPaint = Paint().apply { color = colorNavyLight; style = Paint.Style.FILL }
        canvas.drawRoundRect(badgeRect, 8f, 8f, logoPaint)

        val logoTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorBronze
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("PPP", badgeRect.centerX(), badgeRect.centerY() + 6f, logoTextPaint)

        val brandX = badgeRect.right + 12f
        val businessNamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorNavy
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val businessNameText = if (invoiceData.businessName.trim().endsWith("Ltd", ignoreCase = true)) {
            invoiceData.businessName.trim()
        } else {
            "${invoiceData.businessName.trim()} Ltd"
        }
        canvas.drawText(businessNameText, brandX, y + 18f, businessNamePaint)

        val infoLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = localMediumGray
            textSize = 9f
            letterSpacing = 0.05f
        }
        val infoValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = localTextGray
            textSize = 11f
        }

        var contactY = y + 78f
        fun drawContactInfo(label: String, value: String) {
            canvas.drawText(label, margin, contactY, infoLabelPaint)
            contactY += 12f
            drawWrappedPlainText(
                canvas = canvas,
                text = value.ifBlank { "N/A" },
                x = margin,
                y = contactY,
                maxWidth = 155f,
                paint = infoValuePaint,
                lineHeight = 12f,
                maxLines = 2
            )
            contactY += 20f
        }

        drawContactInfo("Address:", invoiceData.businessAddress)
        drawContactInfo("Contact:", invoiceData.businessPhone)
        drawContactInfo("Email:", invoiceData.businessEmail)

        val centerX = pageWidth / 2f
        val invoiceTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorNavy
            textSize = 30f
            letterSpacing = 0.28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }

        val titleY = y + 86f
        canvas.drawText("INVOICE", centerX, titleY, invoiceTitlePaint)

        val ornamentLinePaint = Paint().apply { color = localBorderGray; strokeWidth = 1f }
        val diamondPaint = Paint().apply { color = colorNavy; style = Paint.Style.FILL }

        fun drawDiamond(cx: Float, cy: Float) {
            canvas.save()
            canvas.rotate(45f, cx, cy)
            canvas.drawRect(cx - 3.5f, cy - 3.5f, cx + 3.5f, cy + 3.5f, diamondPaint)
            canvas.restore()
        }

        val lineYTop = titleY - 34f
        canvas.drawLine(centerX - 92f, lineYTop, centerX + 72f, lineYTop, ornamentLinePaint)
        drawDiamond(centerX + 82f, lineYTop)

        val lineYBottom = titleY + 18f
        canvas.drawLine(centerX - 72f, lineYBottom, centerX + 92f, lineYBottom, ornamentLinePaint)
        drawDiamond(centerX - 82f, lineYBottom)

        val rightX = pageWidth - margin
        val fieldLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = localMediumGray
            textSize = 9f
            textAlign = Paint.Align.RIGHT
            letterSpacing = 0.05f
        }
        val fieldValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorNavy
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }

        var fieldY = y + 8f
        fun drawInfoField(label: String, value: String) {
            canvas.drawText(label.uppercase(), rightX, fieldY, fieldLabelPaint)
            fieldY += 14f
            canvas.drawText(value.ifBlank { "N/A" }, rightX, fieldY, fieldValuePaint)
            fieldY += 28f
        }

        drawInfoField("Invoice Number", invoiceData.invoiceNumber)
        drawInfoField("Invoice Date", invoiceData.issueDate)
        invoiceData.dueDate?.let { drawInfoField("Due Date", it) }

        y = max(contactY, fieldY) + 4f

        val sectionBgPaint = Paint().apply { color = colorLightGray1; style = Paint.Style.FILL }
        val billSectionHeight = 66f
        canvas.drawRect(margin, y, pageWidth - margin, y + billSectionHeight, sectionBgPaint)

        val sectionHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorNavyLight
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.05f
        }
        val bulletPaint = Paint().apply { color = colorBronze; style = Paint.Style.FILL }

        val sectionY = y + 22f
        canvas.drawCircle(margin + 4f, sectionY - 4f, 4f, bulletPaint)
        canvas.drawText("BILL TO", margin + 18f, sectionY, sectionHeaderPaint)

        val billToNamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorNavy
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val billToAddrPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = localTextGray
            textSize = 11f
        }

        canvas.drawText(invoiceData.billTo.ifBlank { "N/A" }, margin + 18f, sectionY + 20f, billToNamePaint)
        drawWrappedPlainText(
            canvas,
            invoiceData.billToAddress.ifBlank { "N/A" },
            margin + 18f,
            sectionY + 36f,
            contentWidth - 36f,
            billToAddrPaint,
            13f,
            maxLines = 2
        )

        return y + billSectionHeight + 28f
    }

    private fun drawInvoiceContinuationHeader(
        canvas: Canvas,
        invoiceData: InvoiceData,
        pageNumber: Int,
        totalPages: Int
    ) {
        val colorNavy = Color.parseColor("#1E293B")
        val colorBronze = Color.parseColor("#9D8560")
        val localTextGray = Color.parseColor("#64748B")

        val topBorderPaint = Paint().apply { color = colorNavy; style = Paint.Style.FILL }
        canvas.drawRect(margin, margin, pageWidth - margin, margin + 4f, topBorderPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorNavy
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = localTextGray
            textSize = 10f
        }
        val linePaint = Paint().apply { color = colorBronze; strokeWidth = 1f }

        val titleY = margin + 28f
        canvas.drawText("INVOICE", margin, titleY, titlePaint)
        canvas.drawText("${invoiceData.invoiceNumber}  •  Page $pageNumber of $totalPages", margin, titleY + 16f, subPaint)
        canvas.drawLine(margin, titleY + 28f, pageWidth - margin, titleY + 28f, linePaint)
    }

    private fun drawInvoiceItemsSectionHeader(canvas: Canvas, y: Float) {
        val colorNavyLight = Color.parseColor("#334155")
        val colorBronze = Color.parseColor("#9D8560")

        val sectionHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorNavyLight
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.05f
        }
        val bulletPaint = Paint().apply { color = colorBronze; style = Paint.Style.FILL }

        canvas.drawCircle(margin + 4f, y - 4f, 4f, bulletPaint)
        canvas.drawText("INVOICE ITEMS", margin + 18f, y, sectionHeaderPaint)
    }

    private fun drawInvoiceItemsTableHeader(canvas: Canvas, y: Float) {
        val colorNavyLight = Color.parseColor("#334155")

        val tableHeadPaint = Paint().apply { color = colorNavyLight; style = Paint.Style.FILL }
        canvas.drawRect(margin, y, pageWidth - margin, y + invoiceTableHeaderHeight, tableHeadPaint)

        val tableHeaderTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorWhite
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val colDesc = margin + 16f
        val colQty = margin + 332f
        val colRate = margin + 435f
        val colAmount = pageWidth - margin - 16f
        val textY = y + 21f

        canvas.drawText("DESCRIPTION", colDesc, textY, tableHeaderTextPaint)
        drawTextRight(canvas, "QTY", colQty, textY, tableHeaderTextPaint)
        drawTextRight(canvas, "RATE", colRate, textY, tableHeaderTextPaint)
        drawTextRight(canvas, "AMOUNT", colAmount, textY, tableHeaderTextPaint)
    }

    private fun drawInvoiceItemRow(
        canvas: Canvas,
        y: Float,
        description: String,
        quantity: Number,
        isLabour: Boolean,
        rate: Double,
        amount: Double,
        striped: Boolean
    ) {
        val colorNavy = Color.parseColor("#1E293B")

        val rowItemPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorNavy
            textSize = 12f
        }
        val dividerPaint = Paint().apply {
            color = colorBorderGray
            strokeWidth = 0.6f
        }
        val stripePaint = Paint().apply {
            color = Color.parseColor("#FAFAF8")
            style = Paint.Style.FILL
        }

        val colDesc = margin + 16f
        val colQty = margin + 332f
        val colRate = margin + 435f
        val colAmount = pageWidth - margin - 16f

        if (striped) {
            canvas.drawRect(margin, y, pageWidth - margin, y + invoiceRowHeight, stripePaint)
        }

        val baselineY = y + 18f
        val qtyText = if (isLabour) "${quantity} hrs" else quantity.toString()

        canvas.drawText(
            ellipsizeToWidth(description.ifBlank { "-" }, rowItemPaint, 290f),
            colDesc,
            baselineY,
            rowItemPaint
        )
        drawTextRight(canvas, qtyText, colQty, baselineY, rowItemPaint)
        drawTextRight(canvas, CurrencyFormatUtils.formatCurrency(rate), colRate, baselineY, rowItemPaint)
        drawTextRight(canvas, CurrencyFormatUtils.formatCurrency(amount), colAmount, baselineY, rowItemPaint)

        canvas.drawLine(margin, y + invoiceRowHeight, pageWidth - margin, y + invoiceRowHeight, dividerPaint)
    }

    private fun drawInvoiceTotals(canvas: Canvas, yStart: Float, invoiceData: InvoiceData) {
        val colorBronze = Color.parseColor("#9D8560")
        val colorNavy = Color.parseColor("#1E293B")
        val colorLightGray1 = Color.parseColor("#F7F5F2")

        var y = yStart + 5f

        val totalLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorTextGray
            textSize = 12f
        }
        val totalValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorNavy
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val dividerPaint = Paint().apply {
            color = colorBorderGray
            strokeWidth = 0.6f
        }

        val colAmount = pageWidth - margin - 16f
        val totalColX = pageWidth - margin - 150f

        canvas.drawText("Subtotal:", totalColX, y, totalLabelPaint)
        drawTextRight(canvas, CurrencyFormatUtils.formatCurrency(invoiceData.subtotal), colAmount, y, totalValuePaint)

        if (invoiceData.includeGst) {
            y += 20f
            canvas.drawLine(totalColX, y - 12f, colAmount, y - 12f, dividerPaint)
            canvas.drawText("GST (${(invoiceData.gstRate * 100).toInt()}%):", totalColX, y, totalLabelPaint)
            drawTextRight(canvas, CurrencyFormatUtils.formatCurrency(invoiceData.gstAmount), colAmount, y, totalValuePaint)
        }

        y += 18f

        val grandTotalBoxHeight = 42f
        val grandTotalBoxWidth = 230f
        val grandTotalBoxRect = RectF(pageWidth - margin - grandTotalBoxWidth, y, pageWidth - margin, y + grandTotalBoxHeight)
        val sectionBgPaint = Paint().apply { color = colorLightGray1; style = Paint.Style.FILL }
        canvas.drawRect(grandTotalBoxRect, sectionBgPaint)

        val grandTotalLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorBronze
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val grandTotalValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorBronze
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        canvas.drawText("TOTAL:", grandTotalBoxRect.left + 16f, y + 27f, grandTotalLabelPaint)
        drawTextRight(
            canvas,
            CurrencyFormatUtils.formatCurrency(invoiceData.total),
            grandTotalBoxRect.right - 16f,
            y + 27f,
            grandTotalValuePaint
        )
    }

    private fun drawInvoicePaymentInformation(canvas: Canvas, y: Float, invoiceData: InvoiceData) {
        val colorBronze = Color.parseColor("#9D8560")
        val colorNavy = Color.parseColor("#1E293B")
        val colorNavyLight = Color.parseColor("#334155")
        val colorLightGray1 = Color.parseColor("#F7F5F2")

        val sectionHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorNavyLight
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.05f
        }
        val bulletPaint = Paint().apply { color = colorBronze; style = Paint.Style.FILL }

        canvas.drawCircle(margin + 4f, y - 4f, 4f, bulletPaint)
        canvas.drawText("PAYMENT INFORMATION", margin + 18f, y, sectionHeaderPaint)

        val boxTop = y + 14f
        val paymentBoxRect = RectF(margin, boxTop, pageWidth - margin, boxTop + 68f)
        val sectionBgPaint = Paint().apply { color = colorLightGray1; style = Paint.Style.FILL }
        canvas.drawRect(paymentBoxRect, sectionBgPaint)

        val paymentLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorNavy
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val paymentValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorTextGray
            textSize = 11f
        }

        var payY = boxTop + 20f
        canvas.drawText("Bank:", margin + 16f, payY, paymentLabelPaint)
        canvas.drawText(invoiceData.accountNumber.ifBlank { "N/A" }, margin + 60f, payY, paymentValuePaint)

        payY += 19f
        canvas.drawText("Account Name:", margin + 16f, payY, paymentLabelPaint)
        canvas.drawText(invoiceData.businessName.ifBlank { "N/A" }, margin + 110f, payY, paymentValuePaint)

        payY += 19f
        canvas.drawText("Account Number:", margin + 16f, payY, paymentLabelPaint)
        canvas.drawText(invoiceData.accountNumber.ifBlank { "N/A" }, margin + 120f, payY, paymentValuePaint)
    }

    private fun drawInvoiceFooter(canvas: Canvas, pageNumber: Int, totalPages: Int) {
        val footerY = pageHeight - margin

        val footerBorderPaint = Paint().apply {
            color = colorBorderGray
            strokeWidth = 1f
        }
        canvas.drawLine(margin, footerY - 16f, pageWidth - margin, footerY - 16f, footerBorderPaint)

        val footerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorMediumGray
            textSize = 10f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "Generated by Pro Painters Plasterers — Page $pageNumber of $totalPages",
            pageWidth / 2f,
            footerY,
            footerTextPaint
        )
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
                    if (result.size == maxLines) {
                        return result.dropLast(1) + ellipsizeToWidth(result.last(), paint, maxWidth)
                    }
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

    /**
     * NEW: Dedicated Luxury Header for Timesheet Preview.
     * This fixes the vertical text wrapping and overlapping titles seen in the preview.
     */
    fun drawTimesheetLuxuryHeader(
        canvas: Canvas,
        businessName: String,
        customerName: String,
        address: String,
        date: String,
        y: Float
    ) {
        val colorNavy = Color.parseColor("#1E293B")
        val colorTextGray = Color.parseColor("#64748B")

        // 1. DIMENSIONS: Widened to prevent vertical wrapping
        val leftColX = margin
        val leftColWidth = 220f // FIXED: Increased from narrow value

        val rightColWidth = 160f
        val rightColX = pageWidth - margin - rightColWidth

        // 2. Business Info (Left Column)
        val businessPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorNavy
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        // drawWrappedPlainText now uses 220f to allow horizontal text
        drawWrappedPlainText(
            canvas,
            businessName.ifBlank { "Pro Painters & Plasterers" },
            leftColX + 55f, // Offset for the logo badge
            y + 12f,
            leftColWidth,
            businessPaint,
            15f
        )

        // 3. TIMESHEET Title (Centered properly to avoid overlap)
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorTextGray
            textSize = 28f
            letterSpacing = 0.15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val titleText = "TIMESHEET"
        val titleWidth = titlePaint.measureText(titleText)
        canvas.drawText(
            titleText,
            (pageWidth / 2f) - (titleWidth / 2f),
            y + 55f,
            titlePaint
        )

        // 4. Job Details (Right Column)
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorTextGray
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorNavy
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        // Draw Name
        canvas.drawText("NAME", rightColX, y + 10f, labelPaint)
        drawWrappedPlainText(canvas, customerName, rightColX, y + 24f, rightColWidth, valuePaint, 12f, 1)

        // Draw Address
        canvas.drawText("ADDRESS", rightColX, y + 55f, labelPaint)
        drawWrappedPlainText(canvas, address, rightColX, y + 69f, rightColWidth, valuePaint, 12f, 2)

        // Draw Date
        canvas.drawText("ISSUE DATE", rightColX, y + 105f, labelPaint)
        canvas.drawText(date, rightColX, y + 119f, valuePaint)
    }

    /**
     * NEW: Dedicated Footer for Luxury View
     */
    fun drawTimesheetLuxuryFooter(canvas: Canvas, pageNumber: Int, totalPages: Int) {
        val footerY = pageHeight - margin
        val footerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            textSize = 10f
            textAlign = Paint.Align.CENTER
        }

        canvas.drawText(
            "Generated by Pro Painters — Page $pageNumber of $totalPages",
            pageWidth / 2f,
            footerY,
            footerTextPaint
        )
    }
}

