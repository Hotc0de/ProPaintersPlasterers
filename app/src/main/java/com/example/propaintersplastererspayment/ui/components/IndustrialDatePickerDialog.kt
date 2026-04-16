package com.example.propaintersplastererspayment.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.example.propaintersplastererspayment.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndustrialDatePickerDialog(
    initialTimestamp: Long,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialTimestamp
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { onDateSelected(it) }
                }
            ) {
                Text("OK", color = IndustrialGold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        },
        colors = DatePickerDefaults.colors(
            containerColor = CharcoalSecondary
        )
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                todayContentColor = IndustrialGold,
                selectedDayContainerColor = IndustrialGold,
                selectedDayContentColor = CharcoalBackground,
                titleContentColor = IndustrialGold,
                headlineContentColor = IndustrialGold,
                weekdayContentColor = TextMuted,
                dayContentColor = OffWhite
            )
        )
    }
}
