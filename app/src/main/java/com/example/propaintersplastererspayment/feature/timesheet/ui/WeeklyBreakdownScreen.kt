package com.example.propaintersplastererspayment.feature.timesheet.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.propaintersplastererspayment.ProPaintersApplication
import com.example.propaintersplastererspayment.core.util.DateFormatUtils
import com.example.propaintersplastererspayment.core.util.WorkEntryTimeUtils
import com.example.propaintersplastererspayment.data.local.entity.WorkEntryEntity
import com.example.propaintersplastererspayment.feature.timesheet.vm.TimesheetViewModel
import com.example.propaintersplastererspayment.ui.components.IndustrialCard
import com.example.propaintersplastererspayment.ui.theme.*
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyBreakdownRoute(
    jobId: Long,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val application = context.applicationContext as ProPaintersApplication
    val viewModel: TimesheetViewModel = viewModel(
        factory = TimesheetViewModel.provideFactory(
            jobId = jobId,
            jobRepository = application.container.jobRepository,
            workEntryRepository = application.container.workEntryRepository,
            materialRepository = application.container.materialRepository,
            settingsRepository = application.container.settingsRepository
        )
    )
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Labor Breakdown", color = IndustrialGold, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = IndustrialGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CharcoalBackground)
            )
        },
        containerColor = CharcoalBackground,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        val breakdown = remember(uiState.entries) {
            calculateWeeklyLaborBreakdown(uiState.entries)
        }

        if (breakdown.weeklySummaries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("No labor recorded yet.", color = TextMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(breakdown.weeklySummaries) { summary ->
                    WeeklyBreakdownCard(summary)
                }
            }
        }
    }
}

@Composable
fun WeeklyBreakdownCard(summary: WeeklyLaborSummary) {
    IndustrialCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            val daysLabel = if (summary.uniqueDaysCount == 1) "day" else "days"
            Text(
                text = "Week ${summary.weekNumber} (${summary.uniqueDaysCount} $daysLabel)",
                style = MaterialTheme.typography.titleMedium,
                color = IndustrialGold,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = summary.dateRange,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = WorkEntryTimeUtils.formatHours(summary.totalHours),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = OffWhite
            )
            Text(
                text = "Total Weekly Hours",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = BorderColor, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(12.dp))

            summary.workerBreakdown.forEach { (worker, hours) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = worker, color = OffWhite, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = WorkEntryTimeUtils.formatHours(hours),
                        color = IndustrialGold,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

data class WeeklyLaborSummary(
    val weekNumber: Int,
    val dateRange: String,
    val totalHours: Double,
    val uniqueDaysCount: Int,
    val workerBreakdown: List<Pair<String, Double>>
)

data class WeeklyLaborBreakdown(
    val totalDays: Int,
    val weeklySummaries: List<WeeklyLaborSummary>
)

fun calculateWeeklyLaborBreakdown(entries: List<WorkEntryEntity>): WeeklyLaborBreakdown {
    if (entries.isEmpty()) return WeeklyLaborBreakdown(0, emptyList())

    val totalDays = entries.map { it.workDate }.distinct().size

    // 1. Sort entries by date
    val sortedEntries = entries.sortedBy { it.workDate }
    val firstEntryDate = DateFormatUtils.parseStoredDate(sortedEntries.first().workDate) ?: return WeeklyLaborBreakdown(totalDays, emptyList())

    // Use Calendar to determine the start of the first week (Monday)
    val calendar = Calendar.getInstance(Locale.getDefault()).apply {
        firstDayOfWeek = Calendar.MONDAY
    }

    // Function to get the Monday of the week for a given date
    fun getStartOfWeek(date: Date): Long {
        calendar.time = date
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    // Group entries by their week start
    val entriesByWeekStart = sortedEntries.groupBy { entry ->
        val date = DateFormatUtils.parseStoredDate(entry.workDate) ?: Date(0)
        getStartOfWeek(date)
    }

    val summaries = entriesByWeekStart.entries
        .sortedBy { it.key }
        .mapIndexed { index, entry ->
            val weekStartLong = entry.key
            val weekEntries = entry.value
            
            calendar.timeInMillis = weekStartLong
            val startDate = calendar.time
            calendar.add(Calendar.DAY_OF_YEAR, 6)
            val endDate = calendar.time

            val dateRange = "${DateFormatUtils.formatTimestampToDisplay(startDate.time)} - ${DateFormatUtils.formatTimestampToDisplay(endDate.time)}"
            val totalHours = weekEntries.sumOf { it.hoursWorked }
            val uniqueDaysCount = weekEntries.map { it.workDate }.distinct().size
            val workerBreakdown = weekEntries.groupBy { it.workerName }
                .mapValues { it.value.sumOf { entry -> entry.hoursWorked } }
                .toList()
                .sortedByDescending { it.second }

            WeeklyLaborSummary(
                weekNumber = index + 1,
                dateRange = dateRange,
                totalHours = totalHours,
                uniqueDaysCount = uniqueDaysCount,
                workerBreakdown = workerBreakdown
            )
        }

    return WeeklyLaborBreakdown(totalDays, summaries)
}
