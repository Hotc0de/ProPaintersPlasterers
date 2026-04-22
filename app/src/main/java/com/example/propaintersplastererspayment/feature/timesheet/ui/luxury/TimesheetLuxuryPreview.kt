package com.example.propaintersplastererspayment.feature.timesheet.ui.luxury

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.core.graphics.withSave
import androidx.core.graphics.toColorInt
import com.example.propaintersplastererspayment.core.pdf.PdfBusinessDetails
import com.example.propaintersplastererspayment.core.pdf.PdfExportService
import com.example.propaintersplastererspayment.core.pdf.TimesheetPdfData
import com.example.propaintersplastererspayment.core.pdf.TimesheetPdfRenderer
import com.example.propaintersplastererspayment.core.pdf.WorkEntryPdfRow
import com.example.propaintersplastererspayment.core.pdf.MaterialPdfRow
import com.example.propaintersplastererspayment.feature.timesheet.vm.TimesheetUiState
import com.example.propaintersplastererspayment.core.util.DateFormatUtils

@Composable
fun TimesheetLuxuryPreview(
    uiState: TimesheetUiState,
    modifier: Modifier = Modifier
) {
    val pdfExportService = PdfExportService()
    
    // Mapping UI state to PDF data for use with the renderer
    val pdfData = TimesheetPdfData(
        fileName = "Preview",
        exportedAt = DateFormatUtils.formatTimestampToDisplay(System.currentTimeMillis()),
        business = PdfBusinessDetails(
            businessName = "Pro Painters & Plasterers",
            address = "170 Tancred Street",
            phoneNumber = "022-10701719",
            email = "painter@gmail.com",
            gstNumber = "???",
            bankAccountNumber = "???",
            bankName = "???"
        ),
        jobName = uiState.job?.jobName ?: "N/A",
        jobAddress = uiState.job?.propertyAddress ?: "N/A",
        workEntries = uiState.entries.map { 
            WorkEntryPdfRow(
                workDate = DateFormatUtils.formatDisplayDate(it.workDate),
                workerName = it.workerName,
                startTime = it.startTime,
                finishTime = it.finishTime,
                hoursWorked = it.hoursWorked
            )
        },
        totalHours = uiState.totalHours,
        materials = uiState.materials.map {
            MaterialPdfRow(
                materialName = it.materialName,
                price = it.price
            )
        },
        totalMaterialCost = uiState.totalMaterialCost
    )

    val renderer = TimesheetPdfRenderer(
        data = pdfData,
        pageWidth = 595,
        pageHeight = 842,
        margin = 40f
    )

    // Using a Box to provide a white background and some elevation/shadow-like effect if needed
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.707f) // A4 Aspect Ratio
            .background(Color.White)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawContext.canvas.nativeCanvas.withSave {
                // Scale the canvas to fit the A4 document dimensions used in PdfExportService
                val scale = size.width / 595f
                scale(scale, scale)
                
                // Draw white background
                val bgPaint = Paint().apply {
                    color = android.graphics.Color.WHITE
                    style = Paint.Style.FILL
                }
                drawRect(0f, 0f, 595f, 842f, bgPaint)
                
                // Draw top bar
                val darkBarPaint = Paint().apply {
                    color = "#1E293B".toColorInt()
                    style = Paint.Style.FILL
                }
                drawRect(0f, 0f, 595f, 4f, darkBarPaint)

                // 1. Draw Header
                var currentY = 20f
                currentY = renderer.drawHeader(this, currentY, isFirstPage = true)

                // 2. Draw Body Content
                // Draw Work Entries
                renderer.drawSectionTitle(this, currentY, "Work Entries")
                currentY += 10f

                renderer.drawWorkEntriesTableHeader(this, currentY)
                currentY += TimesheetPdfRenderer.TABLE_HEADER_HEIGHT

                pdfData.workEntries.forEachIndexed { index, entry ->
                    renderer.drawWorkEntryRow(this, currentY, entry, index % 2 != 0)
                    currentY += TimesheetPdfRenderer.TABLE_ROW_HEIGHT
                }

                renderer.drawTotalHours(this, currentY, pdfData.totalHours)
                currentY += TimesheetPdfRenderer.TABLE_TOTAL_HEIGHT + 20f

                // Draw Materials if any
                if (pdfData.materials.isNotEmpty()) {
                    renderer.drawSectionTitle(this, currentY, "Materials")
                    currentY += 10f

                    renderer.drawMaterialsTableHeader(this, currentY)
                    currentY += TimesheetPdfRenderer.TABLE_HEADER_HEIGHT

                    pdfData.materials.forEachIndexed { index, material ->
                        renderer.drawMaterialRow(this, currentY, material, index % 2 != 0)
                        currentY += TimesheetPdfRenderer.TABLE_ROW_HEIGHT
                    }

                    renderer.drawTotalMaterialCost(this, currentY, pdfData.totalMaterialCost)
                }
                
                // 3. Draw Footer
                renderer.drawFooter(this, 1, 1)
            }
        }
    }
}
