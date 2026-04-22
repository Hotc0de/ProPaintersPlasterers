package com.example.propaintersplastererspayment.feature.timesheet.ui.luxury

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.propaintersplastererspayment.core.util.DateFormatUtils
import com.example.propaintersplastererspayment.core.util.WorkEntryTimeUtils
import com.example.propaintersplastererspayment.data.local.entity.JobEntity
import com.example.propaintersplastererspayment.data.local.entity.WorkEntryEntity
import com.example.propaintersplastererspayment.data.local.entity.MaterialItemEntity
import com.example.propaintersplastererspayment.feature.timesheet.vm.TimesheetUiState
import java.util.Locale

// Colors matching the specification
object TimesheetColors {
    val DarkSlate = Color(0xFF334155)
    val GoldAccent = Color(0xFFCA8A04)
    val White = Color(0xFFFFFFFF)
    val OuterBackground = Color(0xFFF3F4F6)
    val AlternatingRow = Color(0xFFF3F4F6)
    val GrayLabel = Color(0xFF94A3B8)
    val BorderSlate = Color(0xFFCBD5E1)
}

@Composable
fun LuxuryTimesheet(
    uiState: TimesheetUiState,
    modifier: Modifier = Modifier,
    materials: List<MaterialItemEntity> = emptyList(),
    totalMaterialCost: Double = 0.0
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(TimesheetColors.OuterBackground)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(TimesheetColors.White)
        ) {
            // Dark slate top border (4px)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(TimesheetColors.DarkSlate)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
            ) {
                TimesheetHeader(uiState)
                Spacer(modifier = Modifier.height(48.dp))
                WorkEntriesSection(uiState.entries, uiState.totalHours)
                Spacer(modifier = Modifier.height(32.dp))
                MaterialsSection(materials, totalMaterialCost)
                Spacer(modifier = Modifier.height(48.dp))
                TimesheetFooter()
            }
        }
    }
}

@Composable
private fun TimesheetHeader(uiState: TimesheetUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        // Left Column: Logo & Company Info
        Column(modifier = Modifier.weight(1.2f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(TimesheetColors.DarkSlate, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "PPP",
                        color = TimesheetColors.GoldAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Pro Painters",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TimesheetColors.DarkSlate
                    )
                    Text(
                        text = "& PLASTERERS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = TimesheetColors.DarkSlate,
                        letterSpacing = 1.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "170 Tancred Street\nP: 022-10701719 | E: painter@gmail.com\nAccount: ????????-????-??",
                fontSize = 11.sp,
                color = TimesheetColors.DarkSlate,
                lineHeight = 16.sp
            )
        }

        // Center Column: Title
        Column(
            modifier = Modifier
                .weight(1.8f)
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Top line with diamond (top right)
                    // The line starts from near center and goes to the right
                    Box(modifier = Modifier.width(200.dp), contentAlignment = Alignment.TopEnd) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.55f)
                                .height(1.dp)
                                .background(TimesheetColors.DarkSlate)
                                .align(Alignment.CenterEnd)
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .rotate(45f)
                                .background(TimesheetColors.DarkSlate)
                                .align(Alignment.CenterEnd)
                                .offset(x = 4.dp)
                        )
                    }
                    
                    Text(
                        text = "TIMESHEET",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 6.sp,
                        color = TimesheetColors.DarkSlate,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    // Bottom line with diamond (bottom left)
                    // The line starts from the left and goes to near center
                    Box(modifier = Modifier.width(200.dp), contentAlignment = Alignment.BottomStart) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.55f)
                                .height(1.dp)
                                .background(TimesheetColors.DarkSlate)
                                .align(Alignment.CenterStart)
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .rotate(45f)
                                .background(TimesheetColors.DarkSlate)
                                .align(Alignment.CenterStart)
                                .offset(x = (-4).dp)
                        )
                    }
                }
            }
        }

        // Right Column: Info
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.End
        ) {
            InfoBlock("NAME", (uiState.job?.jobName ?: "N/A").uppercase())
            Spacer(modifier = Modifier.height(16.dp))
            InfoBlock("ADDRESS", uiState.job?.propertyAddress ?: "N/A")
            Spacer(modifier = Modifier.height(16.dp))
            InfoBlock("ISSUE DATE", DateFormatUtils.formatTimestampToDisplay(System.currentTimeMillis()))
        }
    }
}

@Composable
private fun InfoBlock(label: String, value: String) {
    Column(horizontalAlignment = Alignment.End) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TimesheetColors.GrayLabel,
            letterSpacing = 1.sp
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TimesheetColors.DarkSlate,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun WorkEntriesSection(entries: List<WorkEntryEntity>, totalHours: Double) {
    Column {
        SectionHeader("WORK ENTRIES")
        Spacer(modifier = Modifier.height(16.dp))
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, TimesheetColors.BorderSlate)
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TimesheetColors.DarkSlate)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                TableHeaderText("DATE", Modifier.weight(1f))
                TableHeaderText("WORKER", Modifier.weight(1.5f))
                TableHeaderText("START", Modifier.weight(1f), TextAlign.Center)
                TableHeaderText("FINISH", Modifier.weight(1f), TextAlign.Center)
                TableHeaderText("HOURS", Modifier.weight(1f), TextAlign.End)
            }

            // Data Rows
            entries.forEachIndexed { index, entry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (index % 2 == 1) TimesheetColors.AlternatingRow else TimesheetColors.White)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableBodyText(DateFormatUtils.formatDisplayDate(entry.workDate), Modifier.weight(1f))
                    TableBodyText(entry.workerName, Modifier.weight(1.5f))
                    TableBodyText(entry.startTime, Modifier.weight(1f), textAlign = TextAlign.Center)
                    TableBodyText(entry.finishTime, Modifier.weight(1f), textAlign = TextAlign.Center)
                    TableBodyText(WorkEntryTimeUtils.formatHours(entry.hoursWorked), Modifier.weight(1f), textAlign = TextAlign.End, isBold = true)
                }
                if (index < entries.size - 1) {
                    HorizontalDivider(color = TimesheetColors.BorderSlate.copy(alpha = 0.5f), thickness = 0.5.dp)
                }
            }

            // Total Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TimesheetColors.AlternatingRow)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TOTAL HOURS:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TimesheetColors.GoldAccent,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(32.dp))
                Text(
                    text = WorkEntryTimeUtils.formatHours(totalHours),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TimesheetColors.GoldAccent
                )
            }
        }
    }
}

@Composable
private fun MaterialsSection(materials: List<MaterialItemEntity>, totalCost: Double) {
    Column {
        SectionHeader("MATERIALS")
        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, TimesheetColors.BorderSlate)
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TimesheetColors.DarkSlate)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                TableHeaderText("MATERIAL", Modifier.weight(1f))
                TableHeaderText("PRICE", Modifier.weight(1f), TextAlign.End)
            }

            // Data Rows
            materials.forEachIndexed { index, material ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (index % 2 == 1) TimesheetColors.AlternatingRow else TimesheetColors.White)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableBodyText(material.materialName, Modifier.weight(1f))
                    TableBodyText(String.format(Locale.getDefault(), "$%.2f", material.price), Modifier.weight(1f), textAlign = TextAlign.End)
                }
                if (index < materials.size - 1) {
                    HorizontalDivider(color = TimesheetColors.BorderSlate.copy(alpha = 0.5f), thickness = 0.5.dp)
                }
            }

            // Total Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TimesheetColors.AlternatingRow)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TOTAL MATERIAL COST:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TimesheetColors.GoldAccent,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(32.dp))
                Text(
                    text = String.format(Locale.getDefault(), "$%.2f", totalCost),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TimesheetColors.GoldAccent
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(TimesheetColors.GoldAccent)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TimesheetColors.DarkSlate,
            letterSpacing = 1.5.sp
        )
    }
}

@Composable
private fun TableHeaderText(text: String, modifier: Modifier = Modifier, textAlign: TextAlign = TextAlign.Start) {
    Text(
        text = text,
        modifier = modifier,
        color = Color.White,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        textAlign = textAlign
    )
}

@Composable
private fun TableBodyText(
    text: String, 
    modifier: Modifier = Modifier, 
    textAlign: TextAlign = TextAlign.Start,
    isBold: Boolean = false
) {
    Text(
        text = text,
        modifier = modifier,
        color = TimesheetColors.DarkSlate,
        fontSize = 13.sp,
        fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
        textAlign = textAlign,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun TimesheetFooter() {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
            text = "Generated by Pro Painters – Page 1 of 1",
            fontSize = 11.sp,
            color = TimesheetColors.GrayLabel
        )
    }
}

@Preview(showBackground = true, widthDp = 1000)
@Composable
fun LuxuryTimesheetPreview() {
    val sampleEntries = listOf(
        WorkEntryEntity(entryId = 1, jobOwnerId = 1, workerName = "Linh", workDate = "20-04-2026", startTime = "08:00", finishTime = "19:00", hoursWorked = 11.0),
        WorkEntryEntity(entryId = 2, jobOwnerId = 1, workerName = "Trung", workDate = "20-04-2026", startTime = "09:00", finishTime = "20:00", hoursWorked = 11.0)
    )
    
    val sampleMaterials = listOf(
        MaterialItemEntity(materialId = 1, jobOwnerId = 1, materialName = "Plaster", price = 11.0)
    )

    LuxuryTimesheet(
        uiState = TimesheetUiState(
            entries = sampleEntries,
            totalHours = 22.0,
            job = JobEntity(
                jobId = 1,
                jobName = "Dr.Strange",
                propertyAddress = "72 Atkinson Ave"
            ),
            isLoading = false
        ),
        materials = sampleMaterials,
        totalMaterialCost = 11.0
    )
}
