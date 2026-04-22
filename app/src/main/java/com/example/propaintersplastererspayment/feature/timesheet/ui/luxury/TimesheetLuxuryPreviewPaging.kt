package com.example.propaintersplastererspayment.feature.timesheet.ui.luxury

import android.graphics.Canvas
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.core.graphics.withSave
import com.example.propaintersplastererspayment.core.pdf.MaterialPdfRow
import com.example.propaintersplastererspayment.core.pdf.PdfBusinessDetails
import com.example.propaintersplastererspayment.core.pdf.TimesheetPdfData
import com.example.propaintersplastererspayment.core.pdf.TimesheetPdfRenderer
import com.example.propaintersplastererspayment.core.pdf.WorkEntryPdfRow
import com.example.propaintersplastererspayment.core.util.DateFormatUtils
import com.example.propaintersplastererspayment.feature.timesheet.vm.TimesheetUiState

@Composable
fun TimesheetLuxuryPreviewPaging(
    uiState: TimesheetUiState,
    modifier: Modifier = Modifier
) {
    val pdfData = remember(uiState) {
        TimesheetPdfData(
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
    }

    val renderer = remember {
        TimesheetPdfRenderer(
            data = pdfData,
            pageWidth = 595,
            pageHeight = 842,
            margin = 40f
        )
    }

    val pageContentLayouts = remember(pdfData) {
        calculatePageLayouts(pdfData)
    }

    val pagerState = rememberPagerState(pageCount = { pageContentLayouts.size })

    HorizontalPager(
        state = pagerState,
        modifier = modifier
    ) { pageIndex ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.707f) // A4 Aspect Ratio
                .background(Color.White)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawContext.canvas.nativeCanvas.withSave {
                    val scale = size.width / 595f
                    scale(scale, scale)

                    val pageContent = pageContentLayouts[pageIndex]
                    pageContent.draw(this, renderer, pageIndex + 1, pageContentLayouts.size)
                }
            }
        }
    }
}

private fun calculatePageLayouts(data: TimesheetPdfData): List<PageContent> {
    val layouts = mutableListOf<PageContent>()
    val pageHeight = 842f
    val margin = 40f
    val bottomLimit = pageHeight - margin - TimesheetPdfRenderer.FOOTER_SPACE

    var currentY = margin
    var currentPageWorkEntries = mutableListOf<WorkEntryPdfRow>()
    var currentPageMaterials = mutableListOf<MaterialPdfRow>()
    var isFirstPage = true

    fun finalizePage(showWorkTotal: Boolean, showMaterialTotal: Boolean) {
        layouts.add(
            PageContent(
                isFirstPage = isFirstPage,
                workEntries = currentPageWorkEntries.toList(),
                materials = currentPageMaterials.toList(),
                showWorkEntriesTotal = showWorkTotal,
                showMaterialsTotal = showMaterialTotal,
                totalHours = data.totalHours,
                totalMaterialCost = data.totalMaterialCost
            )
        )
        isFirstPage = false
        currentPageWorkEntries = mutableListOf()
        currentPageMaterials = mutableListOf()
    }

    fun newPage() {
        currentY = margin + TimesheetPdfRenderer.HEADER_HEIGHT_CONT
    }

    // Initial page setup
    currentY += TimesheetPdfRenderer.HEADER_HEIGHT_FIRST

    // Work Entries
    if (data.workEntries.isNotEmpty()) {
        currentY += TimesheetPdfRenderer.SECTION_TITLE_HEIGHT
        currentY += TimesheetPdfRenderer.TABLE_HEADER_HEIGHT

        data.workEntries.forEach { entry ->
            if (currentY + TimesheetPdfRenderer.TABLE_ROW_HEIGHT > bottomLimit) {
                finalizePage(showWorkTotal = false, showMaterialTotal = false)
                newPage()
                currentY += TimesheetPdfRenderer.SECTION_TITLE_HEIGHT
                currentY += TimesheetPdfRenderer.TABLE_HEADER_HEIGHT
            }
            currentPageWorkEntries.add(entry)
            currentY += TimesheetPdfRenderer.TABLE_ROW_HEIGHT
        }

        if (currentY + TimesheetPdfRenderer.TABLE_TOTAL_HEIGHT > bottomLimit) {
            finalizePage(showWorkTotal = false, showMaterialTotal = false)
            newPage()
        }
        currentY += TimesheetPdfRenderer.TABLE_TOTAL_HEIGHT
    }

    // Materials
    if (data.materials.isNotEmpty()) {
        if (currentY + TimesheetPdfRenderer.SECTION_TITLE_HEIGHT + TimesheetPdfRenderer.TABLE_HEADER_HEIGHT > bottomLimit) {
            finalizePage(showWorkTotal = data.workEntries.isNotEmpty() && currentPageWorkEntries.isNotEmpty(), showMaterialTotal = false)
            newPage()
        }
        currentY += TimesheetPdfRenderer.SECTION_TITLE_HEIGHT
        currentY += TimesheetPdfRenderer.TABLE_HEADER_HEIGHT

        data.materials.forEach { material ->
            if (currentY + TimesheetPdfRenderer.TABLE_ROW_HEIGHT > bottomLimit) {
                finalizePage(showWorkTotal = data.workEntries.isNotEmpty() && currentPageWorkEntries.isNotEmpty(), showMaterialTotal = false)
                newPage()
                currentY += TimesheetPdfRenderer.SECTION_TITLE_HEIGHT
                currentY += TimesheetPdfRenderer.TABLE_HEADER_HEIGHT
            }
            currentPageMaterials.add(material)
            currentY += TimesheetPdfRenderer.TABLE_ROW_HEIGHT
        }

        if (currentY + TimesheetPdfRenderer.TABLE_TOTAL_HEIGHT > bottomLimit) {
            finalizePage(showWorkTotal = data.workEntries.isNotEmpty() && currentPageWorkEntries.isNotEmpty(), showMaterialTotal = false)
            newPage()
        }
    }

    // Finalize the last page
    finalizePage(showWorkTotal = data.workEntries.isNotEmpty(), showMaterialTotal = data.materials.isNotEmpty())

    return layouts.filter { it.workEntries.isNotEmpty() || it.materials.isNotEmpty() }
}


private data class PageContent(
    val isFirstPage: Boolean,
    val workEntries: List<WorkEntryPdfRow>,
    val materials: List<MaterialPdfRow>,
    val showWorkEntriesTotal: Boolean,
    val showMaterialsTotal: Boolean,
    val totalHours: Double,
    val totalMaterialCost: Double
) {
    fun draw(canvas: Canvas, renderer: TimesheetPdfRenderer, pageNumber: Int, totalPages: Int) {
        var currentY = renderer.drawHeader(canvas, 40f, isFirstPage)

        if (workEntries.isNotEmpty()) {
            renderer.drawSectionTitle(canvas, currentY, "Work Entries")
            currentY += TimesheetPdfRenderer.SECTION_TITLE_HEIGHT - 10f
            renderer.drawWorkEntriesTableHeader(canvas, currentY)
            currentY += TimesheetPdfRenderer.TABLE_HEADER_HEIGHT
            workEntries.forEachIndexed { index, entry ->
                renderer.drawWorkEntryRow(canvas, currentY, entry, index % 2 != 0)
                currentY += TimesheetPdfRenderer.TABLE_ROW_HEIGHT
            }
            if (showWorkEntriesTotal) {
                renderer.drawTotalHours(canvas, currentY, totalHours)
                currentY += TimesheetPdfRenderer.TABLE_TOTAL_HEIGHT + 20f
            }
        }

        if (materials.isNotEmpty()) {
            renderer.drawSectionTitle(canvas, currentY, "Materials")
            currentY += TimesheetPdfRenderer.SECTION_TITLE_HEIGHT - 10f
            renderer.drawMaterialsTableHeader(canvas, currentY)
            currentY += TimesheetPdfRenderer.TABLE_HEADER_HEIGHT
            materials.forEachIndexed { index, material ->
                renderer.drawMaterialRow(canvas, currentY, material, index % 2 != 0)
                currentY += TimesheetPdfRenderer.TABLE_ROW_HEIGHT
            }
            if (showMaterialsTotal) {
                renderer.drawTotalMaterialCost(canvas, currentY, totalMaterialCost)
            }
        }

        renderer.drawFooter(canvas, pageNumber, totalPages)
    }
}
