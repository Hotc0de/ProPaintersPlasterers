package com.example.propaintersplastererspayment.core.pdf

import android.graphics.*
import com.example.propaintersplastererspayment.core.util.CurrencyFormatUtils

class CalculationPdfRenderer(
    private val data: CalculationPdfData,
    private val pageWidth: Int,
    private val pageHeight: Int,
    private val margin: Float
) {
    companion object {
        const val HEADER_HEIGHT_FIRST = 150f
        const val HEADER_HEIGHT_CONT = 55f
        const val SECTION_TITLE_HEIGHT = 35f
        const val TABLE_HEADER_HEIGHT = 28f
        const val TABLE_ROW_HEIGHT = 22f
        const val TABLE_TOTAL_HEIGHT = 35f
        const val FOOTER_SPACE = 40f
    }

    private val colorNavy = Color.parseColor("#1A1A1B")
    private val colorSlate = Color.parseColor("#334155")
    private val colorGold = Color.parseColor("#FFB800")
    private val colorLabel = Color.parseColor("#A0A0A0")
    private val colorBgLight = Color.parseColor("#F5F5F0")
    private val colorStripe = Color.parseColor("#FAFAF8")
    private val colorBorder = Color.parseColor("#E0E0E0")
    private val colorWhite = Color.WHITE
    private val colorTextNormal = Color.parseColor("#5A5A5C")

    private val paintLabel = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorLabel
        textSize = 9f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        letterSpacing = 0.12f
    }

    private val paintValue = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorNavy
        textSize = 11f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    fun drawHeader(canvas: Canvas, y: Float, isFirstPage: Boolean, pageNumber: Int = 1): Float {
        val decorPaint = Paint().apply { color = colorNavy; style = Paint.Style.FILL }
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 4f, decorPaint)
        
        if (!isFirstPage) return drawContinuationHeader(canvas, y, pageNumber)

        val headerTop = y + 10f
        drawBusinessInfo(canvas, margin, headerTop)
        drawSummaryCard(canvas, pageWidth - margin, headerTop)
        drawTitle(canvas, pageWidth / 2f + 10f, headerTop + 60f)

        return y + HEADER_HEIGHT_FIRST
    }

    private fun drawBusinessInfo(canvas: Canvas, x: Float, y: Float) {
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

        val nameX = x + 54f
        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorNavy
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("Pro Painters", nameX, y + 18f, namePaint)
        
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorGold
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.05f
        }
        canvas.drawText("& PLASTERERS", nameX, y + 34f, subPaint)

        val labelSmall = Paint(paintLabel).apply { textSize = 9f; color = colorSlate }
        val detailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorNavy; textSize = 10f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }

        var currY = y + 62f
        canvas.drawText(data.business.address, x, currY, detailPaint.apply { typeface = Typeface.DEFAULT; color = colorSlate })

        currY += 16f
        canvas.drawText("P: ", x, currY, labelSmall)
        canvas.drawText(data.business.phoneNumber, x + 14f, currY, detailPaint)

        currY += 16f
        canvas.drawText("E: ", x, currY, labelSmall)
        canvas.drawText(data.business.email, x + 14f, currY, detailPaint)
    }

    private fun drawTitle(canvas: Canvas, cx: Float, cy: Float) {
        val titleFontSize = 22f
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorNavy
            textSize = titleFontSize
            typeface = Typeface.create("serif", Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.35f
        }

        val text = "CALCULATION"
        val textWidth = titlePaint.measureText(text)
        val half = textWidth / 2f
        val textCenterY = cy - (titleFontSize * 0.3f)
        val gap = 20f
        val topY = textCenterY - gap
        val bottomY = textCenterY + gap

        val linePaint = Paint().apply { color = colorNavy; strokeWidth = 0.7f; isAntiAlias = true }
        canvas.drawLine(cx - half, topY, cx + half, topY, linePaint)
        canvas.drawLine(cx - half, bottomY, cx + half, bottomY, linePaint)

        val diamondSize = 3.5f
        drawDiamond(canvas, cx + half, topY, diamondSize, colorNavy)
        drawDiamond(canvas, cx - half, bottomY, diamondSize, colorNavy)

        canvas.drawText(text, cx, cy, titlePaint)
    }

    private fun drawSummaryCard(canvas: Canvas, x: Float, y: Float) {
        val leftX = x - 115f + 10f
        var currY = y + 5f

        drawInfoRow(canvas, leftX, currY, "JOB NAME", data.jobName.ifBlank { "N/A" }.uppercase())
        currY += 32f
        drawInfoRow(canvas, leftX, currY, "EXPORTED AT", data.exportedAt)
    }

    private fun drawInfoRow(canvas: Canvas, x: Float, y: Float, label: String, value: String) {
        canvas.drawText(label, x, y, paintLabel.apply { textAlign = Paint.Align.LEFT })
        canvas.drawText(value, x, y + 14f, paintValue.apply { textAlign = Paint.Align.LEFT })
    }

    fun drawSectionTitle(canvas: Canvas, y: Float, title: String) {
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorNavy
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.1f
        }

        val diamondSize = 3f
        val diamondPaint = Paint().apply { color = colorGold; style = Paint.Style.FILL; isAntiAlias = true }
        val cx = margin + 4f
        val cy = y - 4f
        val path = Path().apply {
            moveTo(cx, cy - diamondSize)
            lineTo(cx + diamondSize, cy)
            lineTo(cx, cy + diamondSize)
            lineTo(cx - diamondSize, cy)
            close()
        }
        canvas.drawPath(path, diamondPaint)

        canvas.drawText(title.uppercase(), margin + 14f, y, titlePaint)

        val linePaint = Paint().apply { color = colorGold; strokeWidth = 0.5f }
        val textWidth = titlePaint.measureText(title.uppercase())
        canvas.drawLine(margin + 14f + textWidth + 10f, y - 4f, pageWidth - margin, y - 4f, linePaint)
    }

    fun drawTableHeader(canvas: Canvas, y: Float) {
        val height = TABLE_HEADER_HEIGHT
        canvas.drawRect(margin, y, pageWidth - margin, y + height, Paint().apply { color = colorSlate; style = Paint.Style.FILL })
        
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorWhite; textSize = 9f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); letterSpacing = 0.1f; textAlign = Paint.Align.LEFT
        }
        
        val currY = y + 18f
        canvas.drawText("AREA", margin + 10f, currY, textPaint)
        canvas.drawText("QTY", margin + 150f, currY, textPaint)
        canvas.drawText("RATE", margin + 210f, currY, textPaint)
        canvas.drawText("SUBTOTAL", margin + 280f, currY, textPaint)
        canvas.drawText("GST", margin + 360f, currY, textPaint)
        canvas.drawText("TOTAL", pageWidth - margin - 10f, currY, textPaint.apply { textAlign = Paint.Align.RIGHT })
    }

    fun drawRow(canvas: Canvas, y: Float, item: CalculationPdfRow, isStripe: Boolean) {
        val height = TABLE_ROW_HEIGHT
        if (isStripe) {
            canvas.drawRect(margin, y, pageWidth - margin, y + height, Paint().apply { color = colorStripe; style = Paint.Style.FILL })
        }
        
        val currY = y + 15f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorTextNormal; textSize = 9f; textAlign = Paint.Align.LEFT }
        
        canvas.drawText(item.areaName, margin + 10f, currY, paint.apply { color = colorNavy; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) })
        
        paint.color = colorTextNormal
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("${item.quantity} ${item.unit}", margin + 150f, currY, paint)
        canvas.drawText(CurrencyFormatUtils.formatCurrency(item.costPerUnit), margin + 210f, currY, paint)
        canvas.drawText(CurrencyFormatUtils.formatCurrency(item.subtotal), margin + 280f, currY, paint)
        canvas.drawText(if (item.includeGst) CurrencyFormatUtils.formatCurrency(item.gstAmount) else "N/A", margin + 360f, currY, paint)
        
        paint.textAlign = Paint.Align.RIGHT
        paint.color = colorNavy
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(CurrencyFormatUtils.formatCurrency(item.total), pageWidth - margin - 10f, currY, paint)
    }

    fun drawGrandTotal(canvas: Canvas, y: Float, subtotal: Double, gstTotal: Double, grandTotal: Double) {
        val linePaint = Paint().apply { color = colorGold; strokeWidth = 1.5f }
        canvas.drawLine(pageWidth - margin - 200f, y, pageWidth - margin, y, linePaint)

        var currY = y + 20f
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorSlate
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.RIGHT
        }
        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorNavy
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }

        canvas.drawText("SUBTOTAL (EXCL. GST):", pageWidth - margin - 100f, currY, labelPaint)
        canvas.drawText(CurrencyFormatUtils.formatCurrency(subtotal), pageWidth - margin - 10f, currY, valuePaint)
        
        currY += 16f
        canvas.drawText("TOTAL GST (15%):", pageWidth - margin - 100f, currY, labelPaint)
        canvas.drawText(CurrencyFormatUtils.formatCurrency(gstTotal), pageWidth - margin - 10f, currY, valuePaint)

        currY += 24f
        labelPaint.apply { textSize = 12f; typeface = Typeface.DEFAULT_BOLD; color = colorNavy }
        canvas.drawText("GRAND TOTAL:", pageWidth - margin - 100f, currY, labelPaint)
        canvas.drawText(CurrencyFormatUtils.formatCurrency(grandTotal), pageWidth - margin - 10f, currY, valuePaint.apply { textSize = 14f; color = colorGold })
    }

    fun drawFooter(canvas: Canvas, pageNumber: Int, totalPages: Int) {
        val footerY = pageHeight - margin
        val accentPaint = Paint().apply { color = colorSlate; style = Paint.Style.FILL }
        canvas.drawRect(margin, pageHeight.toFloat() - 3f, pageWidth - margin, pageHeight.toFloat(), accentPaint)

        val linePaint = Paint().apply { color = colorBorder; strokeWidth = 0.5f }
        canvas.drawLine(margin, footerY - 15f, pageWidth - margin, footerY - 15f, linePaint)
        
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorLabel; textSize = 8f; textAlign = Paint.Align.CENTER }
        val text = "Generated by Pro Painters Plasterers – Page $pageNumber of $totalPages"
        canvas.drawText(text, pageWidth / 2f, footerY, paint)
    }

    fun drawContinuationHeader(canvas: Canvas, y: Float, pageNumber: Int): Float {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorNavy; textSize = 14f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
        canvas.drawText("${data.business.businessName} - CALCULATION (Continued)", margin, y + 15f, paint)

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorLabel; textSize = 10f }
        canvas.drawText("Job: ${data.jobName} | Page $pageNumber", margin, y + 30f, subPaint)

        val linePaint = Paint().apply { color = colorGold; strokeWidth = 1f }
        canvas.drawLine(margin, y + 40f, pageWidth - margin, y + 40f, linePaint)

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
