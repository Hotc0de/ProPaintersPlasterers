package com.example.propaintersplastererspayment.feature.job.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.propaintersplastererspayment.R
import com.example.propaintersplastererspayment.feature.invoice.ui.InvoiceRoute
import com.example.propaintersplastererspayment.feature.materials.ui.MaterialsRoute
import com.example.propaintersplastererspayment.feature.timesheet.ui.TimesheetRoute

private enum class JobDetailTab(val titleRes: Int) {
    TIMESHEET(R.string.job_tab_timesheet),
    MATERIALS(R.string.job_tab_materials),
    INVOICE(R.string.job_tab_invoice)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailScreen(
    jobId: Long,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = remember { JobDetailTab.entries }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.job_detail_title, jobId)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(text = stringResource(tab.titleRes)) }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Top
            ) {
                when (tabs[selectedTab]) {
                    JobDetailTab.TIMESHEET -> TimesheetRoute(jobId = jobId)
                    JobDetailTab.MATERIALS -> MaterialsRoute(jobId = jobId)
                    JobDetailTab.INVOICE -> InvoiceRoute(jobId = jobId)
                }
            }

            Text(
                text = stringResource(R.string.job_detail_hint),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

