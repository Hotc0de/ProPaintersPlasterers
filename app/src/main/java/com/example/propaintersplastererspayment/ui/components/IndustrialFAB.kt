package com.example.propaintersplastererspayment.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.propaintersplastererspayment.ui.theme.*

@Composable
fun IndustrialFAB(
    onClick: () -> Unit,
    icon: ImageVector = Icons.Default.Add,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(64.dp),
        shape = AppShapes.large,
        containerColor = IndustrialGold,
        contentColor = CharcoalBackground,
        elevation = FloatingActionButtonDefaults.elevation(16.dp, 20.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(32.dp)
        )
    }
}
