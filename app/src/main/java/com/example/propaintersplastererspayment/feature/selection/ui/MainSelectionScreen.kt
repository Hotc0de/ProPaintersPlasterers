package com.example.propaintersplastererspayment.feature.selection.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.propaintersplastererspayment.ui.components.PrimaryButton
import com.example.propaintersplastererspayment.ui.theme.CharcoalBackground
import com.example.propaintersplastererspayment.ui.theme.IndustrialGold

@Composable
fun MainSelectionScreen(
    onNavigateToInvoice: () -> Unit,
    onNavigateToJobs: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CharcoalBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "PRO PAINTERS\n& PLASTERERS",
                style = MaterialTheme.typography.displaySmall,
                color = IndustrialGold,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            PrimaryButton(
                text = "Invoice",
                onClick = onNavigateToInvoice,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryButton(
                text = "Job",
                onClick = onNavigateToJobs,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
