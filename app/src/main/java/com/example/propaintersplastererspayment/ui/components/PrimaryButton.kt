package com.example.propaintersplastererspayment.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.propaintersplastererspayment.ui.theme.*

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: @Composable (() -> Unit)? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp).fillMaxWidth(),
        enabled = enabled,
        shape = AppShapes.large,
        colors = ButtonDefaults.buttonColors(
            containerColor = IndustrialGold,
            contentColor = CharcoalBackground,
            disabledContainerColor = CharcoalMuted,
            disabledContentColor = TextMuted
        ),
        elevation = ButtonDefaults.buttonElevation(8.dp, 12.dp, 0.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            icon?.invoke()
            if (icon != null) Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
