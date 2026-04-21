package com.example.propaintersplastererspayment.core.pdf

import android.graphics.*
import com.example.propaintersplastererspayment.core.util.CurrencyFormatUtils
import com.example.propaintersplastererspayment.core.util.DateFormatUtils
import com.example.propaintersplastererspayment.core.util.WorkEntryTimeUtils

class TimesheetPdfRenderer(
    private val data: TimesheetPdfData,
    private val pageWidth: Int,
    private val pageHeight: Int,
    private val margin: Float
) {
    // Layout Constants
    companion object {
        const val HEADER_HEIGHT_FIRST = 150f
        const val HEADER_HEIGHT_CONT = 55f
        const val SECTION_TITLE_HEIGHT = 35f
        const val TABLE_HEADER_HEIGHT = 28f
        const val TABLE_ROW_HEIGHT = 22f
        const val TABLE_TOTAL_HEIGHT = 35f
        const val FOOTER_SPACE = 40f
    }

    // Colors
    private val colorNavy = Color.parseColor("#1E293B")
    private val colorSlate = Color.parseColor("#334155")
    private val colorGold = Color.parseColor("#CA8A04")
    private val colorLabel = Color.parseColor("#94A3B8")
    private val colorBgLight = Color.parseColor("#F8FAFC")
    private val colorStripe = Color.parseColor("#F1F5F9")
    private val colorBorder = Color.parseColor("#CBD5E1")
    private val colorWhite = Color.WHITE

    // Header layout tweak: nudge right by a few points for title and right details
    private val headerRightShift = 10f

    // Paints
    private val paintNavyBold = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorNavy
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val paintNavyNormal = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorNavy
    }

    private val paintLabel = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorLabel
        textSize = 6.5f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        letterSpacing = 0.12f
    }

    private val paintValue = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorNavy
        textSize = 11f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    fun drawHeader(canvas: Canvas, y: Float, isFirstPage: Boolean): Float {
        // Draw Top Decor
        val decorPaint = Paint().apply { color = colorNavy; style = Paint.Style.FILL }
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 4f, decorPaint)
        
        if (!isFirstPage) return drawContinuationHeader(canvas, y)

        val headerTop = y + 10f

        // 1. Business Info (Left)
        drawBusinessInfo(canvas, margin, headerTop)

        // 3. Info Card (Right)
        // Draw the summary card left-aligned internally, positioned slightly more to the right.
        drawSummaryCard(canvas, pageWidth - margin, headerTop)

        // 2. Title (Center)
        // Align the TIMESHEET baseline to the job ADDRESS value line in the
        // right summary card. Nudge down slightly to fine-tune vertical alignment.
        // Previously used headerTop + 55f; now move down by 5f.
        // Nudge the centered TIMESHEET title a few points to the right for visual alignment
        drawTimesheetTitle(canvas, pageWidth / 2f + headerRightShift, headerTop + 60f)

        return y + HEADER_HEIGHT_FIRST
    }

    private fun drawBusinessInfo(canvas: Canvas, x: Float, y: Float) {
        // Logo Badge
        val badgeSize = 44f
        val badgeRect = RectF(x, y, x + badgeSize, y + badgeSize)
        val badgePaint = Paint().apply { color = colorNavy; style = Paint.Style.FILL }
        canvas.drawRoundRect(badgeRect, 10f, 10f, badgePaint)
        
        val logoTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorGold
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("PPP", badgeRect.centerX(), badgeRect.centerY() + 5.5f, logoTextPaint)

        // Business Name
        val nameX = x + 54f
        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorNavy
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("Pro Painters", nameX, y + 18f, namePaint)
        
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            // Use the industrial/gold accent colour for the "& PLASTERERS" subtitle
            color = colorGold
            textSize = 12f
            // Make the "& PLASTERERS" text bold to match heading emphasis
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.05f
        }
        canvas.drawText("& PLASTERERS", nameX, y + 34f, subPaint)

        // Address & Contact
        // Increase label and detail sizes for better readability
        val labelSmall = Paint(paintLabel).apply { textSize = 9f; color = colorSlate }
        val detailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorNavy; textSize = 10f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }

        var currY = y + 62f
        // Address (slightly larger and slate color)
        canvas.drawText(data.business.address, x, currY, detailPaint.apply { typeface = Typeface.DEFAULT; color = colorSlate })

        currY += 16f
        // Phone
        canvas.drawText("P: ", x, currY, labelSmall)
        canvas.drawText(data.business.phoneNumber, x + 14f, currY, detailPaint)

        currY += 16f
        // Email
        canvas.drawText("E: ", x, currY, labelSmall)
        canvas.drawText(data.business.email, x + 14f, currY, detailPaint)
    }

    private fun drawTimesheetTitle(canvas: Canvas, cx: Float, cy: Float) {
        // Title paint: smaller and refined so it reads elegant and restrained
        val titleFontSize = 22f
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorNavy
            textSize = titleFontSize
            typeface = Typeface.create("serif", Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            // Slight letter spacing for refined look
            letterSpacing = 0.35f
        }

        // Measure the text width so the ornament lines match the exact length of the word
        val text = "TIMESHEET"
        val textWidth = titlePaint.measureText(text)
        val half = textWidth / 2f

        // Center lines visually around the text for equal spacing.
        // cy is the baseline, so we offset upwards to find the visual center.
        val textCenterY = cy - (titleFontSize * 0.3f)
        // Increase the gap for more breathing room between the word and the
        // thin ornament lines so the header feels less cramped.
        val gap = 20f
        val topY = textCenterY - gap
        val bottomY = textCenterY + gap

        val linePaint = Paint().apply { color = colorNavy; strokeWidth = 0.7f; isAntiAlias = true }

        // Draw top and bottom centered short lines matching the text width
        canvas.drawLine(cx - half, topY, cx + half, topY, linePaint)
        canvas.drawLine(cx - half, bottomY, cx + half, bottomY, linePaint)

        // Diamond markers: top-right and bottom-left as requested
        val diamondSize = 3.5f
        drawDiamond(canvas, cx + half, topY, diamondSize, colorNavy) // Top Right
        drawDiamond(canvas, cx - half, bottomY, diamondSize, colorNavy) // Bottom Left

        // Draw the title
        canvas.drawText(text, cx, cy, titlePaint)
    }

    private fun drawSummaryCard(canvas: Canvas, x: Float, y: Float) {
        // x is the right-edge anchor (pageWidth - margin).
        // Position the left-aligned block slightly closer to the right edge by applying headerRightShift.
        val leftX = x - 115f + headerRightShift
        var currY = y + 5f

        // Draw label above value; both left-aligned to leftX
        drawInfoRow(canvas, leftX, currY, "NAME", data.jobName.ifBlank { "N/A" }.uppercase())
        currY += 32f
        drawInfoRow(canvas, leftX, currY, "ADDRESS", data.jobAddress.ifBlank { "N/A" })
        currY += 32f
        drawInfoRow(canvas, leftX, currY, "ISSUE DATE", data.exportedAt)
    }

    private fun drawInfoRow(canvas: Canvas, x: Float, y: Float, label: String, value: String) {
        // Left-align both the label and the value inside the right block
        canvas.drawText(label, x, y, paintLabel.apply { textAlign = Paint.Align.LEFT })
        canvas.drawText(value, x, y + 14f, paintValue.apply { textAlign = Paint.Align.LEFT })
    }

    fun drawSectionTitle(canvas: Canvas, y: Float, title: String) {
        val dotPaint = Paint().apply { color = colorGold; style = Paint.Style.FILL; isAntiAlias = true }
        canvas.drawCircle(margin + 4f, y - 4f, 3f, dotPaint)
        
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorNavy
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.05f
        }
        canvas.drawText(title.uppercase(), margin + 14f, y, titlePaint)
    }

    fun drawWorkEntriesTableHeader(canvas: Canvas, y: Float) {
        val height = TABLE_HEADER_HEIGHT
        val rect = RectF(margin, y, pageWidth - margin, y + height)
        canvas.drawRect(rect, Paint().apply { color = colorSlate; style = Paint.Style.FILL })
        
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorWhite
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.1f
            textAlign = Paint.Align.LEFT
        }
        
        val currY = y + 18f
        canvas.drawText("DATE", margin + 10f, currY, textPaint)
        canvas.drawText("WORKER", margin + 90f, currY, textPaint)
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("START", margin + 230f, currY, textPaint)
        canvas.drawText("FINISH", margin + 300f, currY, textPaint)
        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("HOURS", pageWidth - margin - 10f, currY, textPaint)
    }

    fun drawWorkEntryRow(canvas: Canvas, y: Float, entry: WorkEntryPdfRow, isStripe: Boolean) {
        val height = TABLE_ROW_HEIGHT
        if (isStripe) {
            canvas.drawRect(margin, y, pageWidth - margin, y + height, Paint().apply { color = colorStripe; style = Paint.Style.FILL })
        }
        
        val currY = y + 15f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorSlate; textSize = 10f; textAlign = Paint.Align.LEFT }
        
        canvas.drawText(entry.workDate, margin + 10f, currY, paint)
        canvas.drawText(entry.workerName, margin + 90f, currY, paint.apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) })
        
        paint.typeface = Typeface.DEFAULT
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(entry.startTime, margin + 230f, currY, paint)
        canvas.drawText(entry.finishTime, margin + 300f, currY, paint)
        
        paint.textAlign = Paint.Align.RIGHT
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(WorkEntryTimeUtils.formatHours(entry.hoursWorked), pageWidth - margin - 10f, currY, paint)
    }

    fun drawTotalHours(canvas: Canvas, y: Float, totalHours: Double) {
        val height = TABLE_TOTAL_HEIGHT
        canvas.drawRect(margin, y, pageWidth - margin, y + height, Paint().apply { color = colorStripe; style = Paint.Style.FILL })
        
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorGold
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            letterSpacing = 0.05f
        }
        canvas.drawText("TOTAL HOURS:", pageWidth - margin - 70f, y + 22f, labelPaint)
        canvas.drawText(WorkEntryTimeUtils.formatHours(totalHours), pageWidth - margin - 15f, y + 22f, labelPaint.apply { textSize = 13f })
    }

    fun drawMaterialsTableHeader(canvas: Canvas, y: Float) {
        val height = TABLE_HEADER_HEIGHT
        canvas.drawRect(margin, y, pageWidth - margin, y + height, Paint().apply { color = colorSlate; style = Paint.Style.FILL })
        
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorWhite; textSize = 9f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); letterSpacing = 0.1f; textAlign = Paint.Align.LEFT
        }
        canvas.drawText("MATERIAL", margin + 10f, y + 18f, textPaint)
        canvas.drawText("PRICE", pageWidth - margin - 10f, y + 18f, textPaint.apply { textAlign = Paint.Align.RIGHT })
    }

    fun drawMaterialRow(canvas: Canvas, y: Float, material: MaterialPdfRow, isStripe: Boolean) {
        val height = TABLE_ROW_HEIGHT
        if (isStripe) {
            canvas.drawRect(margin, y, pageWidth - margin, y + height, Paint().apply { color = colorStripe; style = Paint.Style.FILL })
        }
        val currY = y + 15f
        canvas.drawText(material.materialName, margin + 10f, currY, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorSlate; textSize = 10f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) })
        canvas.drawText(CurrencyFormatUtils.formatCurrency(material.price), pageWidth - margin - 10f, currY, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorSlate; textSize = 10f; textAlign = Paint.Align.RIGHT })
    }

    fun drawTotalMaterialCost(canvas: Canvas, y: Float, totalCost: Double) {
        val height = TABLE_TOTAL_HEIGHT
        canvas.drawRect(margin, y, pageWidth - margin, y + height, Paint().apply { color = colorStripe; style = Paint.Style.FILL })
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorGold; textSize = 10f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); textAlign = Paint.Align.RIGHT; letterSpacing = 0.05f }
        canvas.drawText("TOTAL MATERIAL COST:", pageWidth - margin - 70f, y + 22f, labelPaint)
        canvas.drawText(CurrencyFormatUtils.formatCurrency(totalCost), pageWidth - margin - 15f, y + 22f, labelPaint.apply { textSize = 13f })
    }

    fun drawFooter(canvas: Canvas, pageNumber: Int, totalPages: Int) {
        val y = pageHeight - margin + 12f
        val linePaint = Paint().apply { color = colorBorder; strokeWidth = 0.5f }
        canvas.drawLine(margin, y - 22f, pageWidth - margin, y - 22f, linePaint)
        
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorLabel; textSize = 8f; textAlign = Paint.Align.CENTER }
        canvas.drawText("Generated by Pro Painters – Page $pageNumber of $totalPages", pageWidth / 2f, y, paint)
    }

    private fun drawContinuationHeader(canvas: Canvas, y: Float): Float {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorNavy; textSize = 14f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
        canvas.drawText("${data.business.businessName} - TIMESHEET (Continued)", margin, y + 18f, paint)
        canvas.drawLine(margin, y + 30f, pageWidth - margin, y + 30f, Paint().apply { color = colorGold; strokeWidth = 1f })
        return y + HEADER_HEIGHT_CONT
    }

    private fun drawDiamond(canvas: Canvas, cx: Float, cy: Float, size: Float, color: Int) {
        val path = Path().apply {
            moveTo(cx, cy - size)
            lineTo(cx + size, cy)
            lineTo(cx, cy + size)
            lineTo(cx - size, cy)
            close()
        }
        canvas.drawPath(path, Paint().apply { this.color = color; style = Paint.Style.FILL; isAntiAlias = true })
    }
}
