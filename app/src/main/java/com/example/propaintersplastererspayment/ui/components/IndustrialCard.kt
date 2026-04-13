package com.example.propaintersplastererspayment.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.propaintersplastererspayment.ui.theme.*

@Composable
fun IndustrialCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = AppShapes.large,
            colors = CardDefaults.cardColors(containerColor = CharcoalCard),
            border = BorderStroke(1.dp, BorderColor),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp,
                pressedElevation = 4.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(AppDimensions.cardPadding),
                content = content
            )
        }
    } else {
        Card(
            modifier = modifier,
            shape = AppShapes.large,
            colors = CardDefaults.cardColors(containerColor = CharcoalCard),
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Column(
                modifier = Modifier.padding(AppDimensions.cardPadding),
                content = content
            )
        }
    }
}
