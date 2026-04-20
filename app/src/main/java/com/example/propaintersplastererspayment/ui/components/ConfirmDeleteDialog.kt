package com.example.propaintersplastererspayment.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.propaintersplastererspayment.ui.theme.IndustrialGold

@Composable
fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = Color.White) },
        text = { Text(message, color = Color.White) },
        confirmButton = {
            TextButton(onClick = { onConfirm() }) {
                Text("DELETE", color = Color.Red)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = IndustrialGold)
            }
        },
        containerColor = Color(0xFF1A1A1A)
    )
}

