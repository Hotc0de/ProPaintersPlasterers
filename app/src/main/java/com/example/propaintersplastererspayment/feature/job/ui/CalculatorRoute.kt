package com.example.propaintersplastererspayment.feature.job.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.propaintersplastererspayment.ui.theme.IndustrialGold
import com.example.propaintersplastererspayment.ui.theme.TextMuted

@Composable
fun CalculatorRoute(jobId: Long) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Paint Calculator",
            style = MaterialTheme.typography.headlineMedium,
            color = IndustrialGold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Coming soon: Calculate paint requirements for Job #$jobId",
            style = MaterialTheme.typography.bodyLarge,
            color = TextMuted
        )
    }
}
