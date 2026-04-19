package com.example.propaintersplastererspayment.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.propaintersplastererspayment.core.util.PaintColorUtils

@Composable
fun ColorSwatch(
    hexCode: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    showBorder: Boolean = true
) {
    val color = PaintColorUtils.parseColor(hexCode)
    
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
            .then(
                if (showBorder) {
                    Modifier.border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                } else {
                    Modifier
                }
            )
    )
}
