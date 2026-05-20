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
        val weeklyData = remember(uiState.entries) {
            groupEntriesByWeek(uiState.entries)
        }

        if (weeklyData.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("No labor recorded yet.", color = TextMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(weeklyData) { weekInfo ->
                    WeeklyBreakdownCard(weekInfo)
                }
            }
        }
    }
}

@Composable
fun WeeklyBreakdownCard(weekInfo: WeeklyBreakdownInfo) {
    IndustrialCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Week ${weekInfo.weekNumber}",
                style = MaterialTheme.typography.titleMedium,
                color = IndustrialGold,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = weekInfo.dateRange,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = WorkEntryTimeUtils.formatHours(weekInfo.totalHours),
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

            weekInfo.workerBreakdown.forEach { (worker, hours) ->
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

data class WeeklyBreakdownInfo(
    val weekNumber: Int,
    val dateRange: String,
    val totalHours: Double,
    val workerBreakdown: List<Pair<String, Double>>
)

fun groupEntriesByWeek(entries: List<WorkEntryEntity>): List<WeeklyBreakdownInfo> {
    if (entries.isEmpty()) return emptyList()

    // 1. Sort entries by date
    val sortedEntries = entries.sortedBy { it.workDate }
    val firstEntryDate = DateFormatUtils.parseStoredDate(sortedEntries.first().workDate) ?: return emptyList()

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

    val startOfFirstWeek = getStartOfWeek(firstEntryDate)

    // Group entries by their week start
    val entriesByWeekStart = sortedEntries.groupBy { entry ->
        val date = DateFormatUtils.parseStoredDate(entry.workDate) ?: Date(0)
        getStartOfWeek(date)
    }

    return entriesByWeekStart.entries
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
            val workerBreakdown = weekEntries.groupBy { it.workerName }
                .mapValues { it.value.sumOf { entry -> entry.hoursWorked } }
                .toList()
                .sortedByDescending { it.second }

            WeeklyBreakdownInfo(
                weekNumber = index + 1,
                dateRange = dateRange,
                totalHours = totalHours,
                workerBreakdown = workerBreakdown
            )
        }
}
