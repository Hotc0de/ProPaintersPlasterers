package com.example.propaintersplastererspayment.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = IndustrialGold,
    onPrimary = CharcoalBackground,
    secondary = CharcoalSecondary,
    onSecondary = OffWhite,
    background = CharcoalBackground,
    onBackground = OffWhite,
    surface = CharcoalCard,
    onSurface = OffWhite,
    surfaceVariant = CharcoalMuted,
    onSurfaceVariant = TextMuted,
    error = ErrorRed,
    onError = OffWhite,
    outline = BorderColor
)

@Composable
fun ProPaintersTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
