package com.example.propaintersplastererspayment.feature.job.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.propaintersplastererspayment.ProPaintersApplication
import com.example.propaintersplastererspayment.R
import com.example.propaintersplastererspayment.feature.invoice.ui.InvoiceRoute
import com.example.propaintersplastererspayment.feature.materials.ui.MaterialsRoute
import com.example.propaintersplastererspayment.feature.timesheet.ui.TimesheetRoute
import com.example.propaintersplastererspayment.ui.components.AppLogo
import com.example.propaintersplastererspayment.ui.theme.*

private enum class JobDetailTab(val titleRes: Int) {
    TIMESHEET(R.string.job_tab_timesheet),
    MATERIALS(R.string.job_tab_materials),
    INVOICE(R.string.job_tab_invoice)
}

@Composable
fun JobDetailScreen(
    jobId: Long,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val application = LocalContext.current.applicationContext as ProPaintersApplication
    val job by application.container.jobRepository.observeJob(jobId).collectAsState(initial = null)
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = remember { JobDetailTab.entries }
    val screenTitle = job?.clientName?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.job_detail_title, jobId)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CharcoalBackground,
        topBar = {
            Column(
                modifier = Modifier
                    .background(CharcoalBackground)
                    .padding(top = 16.dp, start = 8.dp, end = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = IndustrialGold
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = screenTitle,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = IndustrialGold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        job?.propertyAddress?.takeIf { it.isNotBlank() }?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                PrimaryScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = CharcoalBackground,
                    contentColor = IndustrialGold,
                    edgePadding = 16.dp,
                    indicator = {
                        TabRowDefaults.PrimaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(selectedTabIndex = selectedTab),
                            color = IndustrialGold,
                            width = 64.dp
                        )
                    },
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = stringResource(tab.titleRes),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            selectedContentColor = IndustrialGold,
                            unselectedContentColor = TextMuted
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                when (tabs[selectedTab]) {
                    JobDetailTab.TIMESHEET -> TimesheetRoute(jobId = jobId)
                    JobDetailTab.MATERIALS -> MaterialsRoute(jobId = jobId)
                    JobDetailTab.INVOICE -> InvoiceRoute(jobId = jobId)
                }
            }
        }
    }
}
