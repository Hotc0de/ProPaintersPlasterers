package com.example.propaintersplastererspayment.core.pdf

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withRotation
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
    private val colorOffWhite = "#FDFCFB".toColorInt()
    private val colorGoldAccent = "#9D8560".toColorInt()
    private val colorLightGray = "#F1F5F9".toColorInt()
    private val colorMediumGray = "#94A3B8".toColorInt()
    private val colorTextGray = "#64748B".toColorInt()
    private val colorBorderGray = "#CBD5E1".toColorInt()
    private val colorWhite = Color.WHITE

    fun exportTimesheetPdf(data: TimesheetPdfData, outputFile: File): File {
        val document = PdfDocument()
        val totalPages = calculateTotalPages(data)
        val cursor = startDocument(document, totalPages)
        val renderer = TimesheetPdfRenderer(data, pageWidth, pageHeight, margin)

        drawTimesheetHeader(cursor, data, isFirstPage = true)

        renderer.drawSectionTitle(cursor.page.canvas, cursor.y, "Work Entries")
        cursor.y += 10f
        drawWorkEntriesTable(cursor, data, renderer)

        if (data.materials.isNotEmpty()) {
            cursor.y += 40f // Add space to push MATERIALS down
            renderer.drawSectionTitle(cursor.page.canvas, cursor.y, "Materials")
            cursor.y += 10f
            drawMaterialsTable(cursor, data, renderer)
        }

        drawFooterLuxury(cursor, renderer)
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
        }

        return pages
    }

    private fun startDocument(document: PdfDocument, totalPages: Int): PageCursor {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        page.canvas.drawColor(colorOffWhite)
        return PageCursor(document = document, page = page, pageNumber = 1, totalPages = totalPages, y = margin)
    }

    private fun finishDocument(cursor: PageCursor) {
        cursor.document.finishPage(cursor.page)
    }

    private fun ensureSpaceLuxury(
        cursor: PageCursor,
        requiredHeight: Float,
        data: TimesheetPdfData,
        renderer: TimesheetPdfRenderer
    ) {
        val bottomLimit = pageHeight - margin - 40f
        if (cursor.y + requiredHeight <= bottomLimit) return

        drawFooterLuxury(cursor, renderer)
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

        val renderer = TimesheetPdfRenderer(data, pageWidth, pageHeight, margin)
        if (isFirstPage) {
            cursor.y = renderer.drawHeader(canvas, yStart, isFirstPage = true)
        } else {
            cursor.y = renderer.drawContinuationHeader(canvas, yStart, cursor.pageNumber)
        }
    }

    private fun drawSectionTitleLuxury(cursor: PageCursor, title: String) {
        val canvas = cursor.page.canvas
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = "#1E293B".toColorInt()
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.1f
        }

        // Draw diamond icon
        val diamondSize = 3f
        val diamondPaint = Paint().apply { color = colorGoldAccent; style = Paint.Style.FILL; isAntiAlias = true }
        val cx = margin + 4f
        val cy = cursor.y - 4f
        val path = Path().apply {
            moveTo(cx, cy - diamondSize)
            lineTo(cx + diamondSize, cy)
            lineTo(cx, cy + diamondSize)
            lineTo(cx - diamondSize, cy)
            close()
        }
        canvas.drawPath(path, diamondPaint)

        canvas.drawText(title.uppercase(), margin + 14f, cursor.y, paint)

        val linePaint = Paint().apply { color = colorGoldAccent; strokeWidth = 0.5f }
        val textWidth = paint.measureText(title.uppercase())
        canvas.drawLine(margin + 14f + textWidth + 10f, cursor.y - 4f, pageWidth - margin, cursor.y - 4f, linePaint)
    }

    private fun drawWorkEntriesTable(cursor: PageCursor, data: TimesheetPdfData, renderer: TimesheetPdfRenderer) {
        renderer.drawWorkEntriesTableHeader(cursor.page.canvas, cursor.y)
        cursor.y += TimesheetPdfRenderer.TABLE_HEADER_HEIGHT

        data.workEntries.forEachIndexed { index, entry ->
            ensureSpaceWithHeader(cursor, data, renderer) {
                renderer.drawWorkEntriesTableHeader(cursor.page.canvas, cursor.y)
                cursor.y += TimesheetPdfRenderer.TABLE_HEADER_HEIGHT
            }

            renderer.drawWorkEntryRow(cursor.page.canvas, cursor.y, entry, index % 2 != 0)
            cursor.y += TimesheetPdfRenderer.TABLE_ROW_HEIGHT
        }

        ensureSpaceLuxury(cursor, TimesheetPdfRenderer.TABLE_TOTAL_HEIGHT, data, renderer)
        renderer.drawTotalHours(cursor.page.canvas, cursor.y, data.totalHours)
        cursor.y += TimesheetPdfRenderer.TABLE_TOTAL_HEIGHT + 10f
    }

    private fun drawWorkEntriesTableHeader(cursor: PageCursor, colDate: Float, colWorker: Float, colStart: Float, colFinish: Float, colHours: Float) {
        val canvas = cursor.page.canvas
        val headerHeight = 25f
        val headerRect = RectF(margin, cursor.y, pageWidth - margin, cursor.y + headerHeight)
        val headerPaint = Paint().apply { color = "#2C3E50".toColorInt(); style = Paint.Style.FILL }
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

    private fun goToNextPage(cursor: PageCursor, data: TimesheetPdfData, renderer: TimesheetPdfRenderer) {
        drawFooterLuxury(cursor, renderer)
        cursor.document.finishPage(cursor.page)
        cursor.pageNumber += 1
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, cursor.pageNumber).create()
        cursor.page = cursor.document.startPage(pageInfo)
        cursor.page.canvas.drawColor(colorOffWhite)
        cursor.y = margin
        drawTimesheetHeader(cursor, data, isFirstPage = false)
        cursor.y += 10f
    }

    private fun ensureSpaceWithHeader(cursor: PageCursor, data: TimesheetPdfData, renderer: TimesheetPdfRenderer, onNewPage: () -> Unit) {
        val bottomLimit = pageHeight - margin - 40f
        if (cursor.y + 22f <= bottomLimit) return

        goToNextPage(cursor, data, renderer)
        onNewPage()
    }

    private fun drawMaterialsTable(cursor: PageCursor, data: TimesheetPdfData, renderer: TimesheetPdfRenderer) {
        renderer.drawMaterialsTableHeader(cursor.page.canvas, cursor.y)
        cursor.y += TimesheetPdfRenderer.TABLE_HEADER_HEIGHT

        data.materials.forEachIndexed { index, material ->
            ensureSpaceLuxury(cursor, TimesheetPdfRenderer.TABLE_ROW_HEIGHT, data, renderer)
            renderer.drawMaterialRow(cursor.page.canvas, cursor.y, material, index % 2 != 0)
            cursor.y += TimesheetPdfRenderer.TABLE_ROW_HEIGHT
        }

        ensureSpaceLuxury(cursor, TimesheetPdfRenderer.TABLE_TOTAL_HEIGHT, data, renderer)
        renderer.drawTotalMaterialCost(cursor.page.canvas, cursor.y, data.totalMaterialCost)
        cursor.y += TimesheetPdfRenderer.TABLE_TOTAL_HEIGHT
    }

    private fun drawFooterLuxury(cursor: PageCursor, renderer: TimesheetPdfRenderer) {
        renderer.drawFooter(cursor.page.canvas, cursor.pageNumber, cursor.totalPages)
    }

    fun exportInvoicePdf(data: InvoicePdfData, outputFile: File): File {
        val invoiceData = InvoiceDataMapper.map(data)
        val document = PdfDocument()

        val totalPages = calculateInvoiceTotalPages(invoiceData)
        val cursor = startInvoiceDocument(document, totalPages)

        renderInvoiceDocument(cursor, invoiceData)

        cursor.document.finishPage(cursor.page)
        writeDocument(document, outputFile)
        return outputFile
    }

    private fun startInvoiceDocument(
        document: PdfDocument,
        totalPages: Int
    ): InvoicePageCursor {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        page.canvas.drawColor(colorWhite)
        return InvoicePageCursor(
            document = document,
            page = page,
            pageNumber = 1,
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
        val colorNavy = "#1E293B".toColorInt()
        val colorNavyLight = "#334155".toColorInt()
        val colorBronze = "#9D8560".toColorInt()
        val colorLightGray1 = "#F7F5F2".toColorInt()
        val localTextGray = "#64748B".toColorInt()
        val localMediumGray = "#94A3B8".toColorInt()
        val localBorderGray = "#CBD5E1".toColorInt()

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
            canvas.withRotation(45f, cx, cy) {
                drawRect(cx - 3.5f, cy - 3.5f, cx + 3.5f, cy + 3.5f, diamondPaint)
            }
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
            13f
        )

        return y + billSectionHeight + 28f
    }

    private fun drawInvoiceContinuationHeader(
        canvas: Canvas,
        invoiceData: InvoiceData,
        pageNumber: Int,
        totalPages: Int
    ) {
        val colorNavy = "#1E293B".toColorInt()
        val colorBronze = "#9D8560".toColorInt()
        val localTextGray = "#64748B".toColorInt()

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
        val colorNavyLight = "#334155".toColorInt()
        val colorBronze = "#9D8560".toColorInt()

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
        val colorNavyLight = "#334155".toColorInt()

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
        val colorNavy = "#1E293B".toColorInt()

        val rowItemPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorNavy
            textSize = 12f
        }
        val dividerPaint = Paint().apply {
            color = colorBorderGray
            strokeWidth = 0.6f
        }
        val stripePaint = Paint().apply {
            color = "#FAFAF8".toColorInt()
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
        val colorBronze = "#9D8560".toColorInt()
        val colorNavy = "#1E293B".toColorInt()
        val colorLightGray1 = "#F7F5F2".toColorInt()

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
        val colorBronze = "#9D8560".toColorInt()
        val colorNavy = "#1E293B".toColorInt()
        val colorNavyLight = "#334155".toColorInt()
        val colorLightGray1 = "#F7F5F2".toColorInt()

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

    private fun drawWrappedPlainText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        maxWidth: Float,
        paint: Paint,
        lineHeight: Float,
        maxLines: Int = 2
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
        maxLines: Int
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

    private fun writeDocument(document: PdfDocument, outputFile: File) {
        outputFile.parentFile?.mkdirs()
        FileOutputStream(outputFile).use { stream ->
            document.writeTo(stream)
        }
        document.close()
    }
}
