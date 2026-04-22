package com.example.propaintersplastererspayment

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.propaintersplastererspayment.core.pdf.PdfExportService
import com.example.propaintersplastererspayment.core.pdf.PdfFileHelper
import com.example.propaintersplastererspayment.core.pdf.TimesheetPdfData
import com.example.propaintersplastererspayment.core.pdf.PdfBusinessDetails
import com.example.propaintersplastererspayment.core.pdf.WorkEntryPdfRow
import com.example.propaintersplastererspayment.core.pdf.MaterialPdfRow
import com.example.propaintersplastererspayment.core.util.DateFormatUtils
import java.io.File

/**
 * Debug-only activity to generate a sample timesheet PDF and open it.
 * Included in the debug source set so it will not be packaged in release builds.
 */
class TimesheetPdfPreviewActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Run generation on a background thread to avoid blocking UI
        Thread {
            try {
                val now = System.currentTimeMillis()
                val business = PdfBusinessDetails(
                    businessName = "Pro Painters & Plasterers",
                    address = "123 Example St, Auckland",
                    phoneNumber = "+64 9 123 4567",
                    email = "info@propainters.nz",
                    gstNumber = "GST123456",
                    bankAccountNumber = "12-3456-7890123-00",
                    bankName = "ANZ Bank"
                )

                val workEntries = listOf(
                    WorkEntryPdfRow(workDate = DateFormatUtils.formatTimestampToDisplay(now), workerName = "John Doe", startTime = "08:00", finishTime = "12:00", hoursWorked = 4.0),
                    WorkEntryPdfRow(workDate = DateFormatUtils.formatTimestampToDisplay(now), workerName = "Jane Smith", startTime = "12:30", finishTime = "17:00", hoursWorked = 4.5)
                )

                val materials = listOf(
                    MaterialPdfRow(materialName = "Paint - White", price = 120.0),
                    MaterialPdfRow(materialName = "Masking Tape", price = 15.5)
                )

                // Use a stable preview filename and clear any existing preview files
                // first so the generated preview is always fresh and avoids viewer
                // caching issues.
                val fileName = "timesheet_preview.pdf"

                // Centralized cache clear helper (deletes previous exported PDFs)
                PdfFileHelper.clearExportCache(this@TimesheetPdfPreviewActivity)

                val data = TimesheetPdfData(
                    fileName = fileName,
                    exportedAt = DateFormatUtils.formatTimestampToDisplay(now),
                    business = business,
                    jobName = "Demo Job",
                    jobAddress = "45 Demo Ave, Auckland",
                    workEntries = workEntries,
                    totalHours = workEntries.sumOf { it.hoursWorked },
                    materials = materials,
                    totalMaterialCost = materials.sumOf { it.price }
                )


                val outFile = PdfFileHelper.createExportFile(this@TimesheetPdfPreviewActivity, data.fileName)
                PdfExportService().exportTimesheetPdf(data, outFile)

                runOnUiThread {
                    Toast.makeText(this@TimesheetPdfPreviewActivity, "Timesheet PDF generated: ${outFile.absolutePath}", Toast.LENGTH_LONG).show()
                    // Try to open the PDF
                    val opened = PdfFileHelper.openPdf(this@TimesheetPdfPreviewActivity, outFile)
                    if (!opened) {
                        Toast.makeText(this@TimesheetPdfPreviewActivity, "PDF created but no app found to open it.", Toast.LENGTH_LONG).show()
                    }
                    finish()
                }

            } catch (t: Throwable) {
                t.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@TimesheetPdfPreviewActivity, "Failed to generate PDF: ${t.message}", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }.start()
    }
}




