package com.example.propaintersplastererspayment.feature.job.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.propaintersplastererspayment.ProPaintersApplication
import com.example.propaintersplastererspayment.R
import com.example.propaintersplastererspayment.ui.theme.IndustrialGold
import com.example.propaintersplastererspayment.ui.theme.TextMuted
import kotlinx.coroutines.launch

@Composable
fun JobNotesRoute(jobId: Long) {
    val application = LocalContext.current.applicationContext as ProPaintersApplication
    val job by application.container.jobRepository.observeJob(jobId).collectAsState(initial = null)
    val scope = rememberCoroutineScope()

    job?.let { currentJob ->
        JobNotesScreen(
            notes = currentJob.notes,
            onNotesChange = { newNotes ->
                scope.launch {
                    application.container.jobRepository.updateJobNotes(jobId, newNotes)
                }
            }
        )
    }
}

@Composable
fun JobNotesScreen(
    notes: String,
    onNotesChange: (String) -> Unit
) {
    var text by remember(notes) { mutableStateOf(notes) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.job_notes_label),
            style = MaterialTheme.typography.titleMedium,
            color = IndustrialGold
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = text,
            onValueChange = { 
                text = it
                onNotesChange(it)
            },
            modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp),
            placeholder = { Text(stringResource(R.string.job_notes_hint), color = TextMuted) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedBorderColor = IndustrialGold,
                unfocusedBorderColor = IndustrialGold.copy(alpha = 0.5f)
            )
        )
    }
}
