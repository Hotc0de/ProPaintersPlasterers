package com.example.propaintersplastererspayment.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.propaintersplastererspayment.ui.theme.IndustrialGold

@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    fontSize: TextUnit = 18.sp,
    cornerRadius: Dp = 8.dp
) {
    Surface(
        modifier = modifier.size(size),
        shape = RoundedCornerShape(cornerRadius),
        color = Color(0xFF0D0D0D) // Deep black similar to the selection screen background
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "PPP",
                color = IndustrialGold,
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                fontSize = fontSize,
                letterSpacing = 1.sp
            )
        }
    }
}
