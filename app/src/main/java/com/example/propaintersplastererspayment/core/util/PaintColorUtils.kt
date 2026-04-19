package com.example.propaintersplastererspayment.core.util

import androidx.compose.ui.graphics.Color

object PaintColorUtils {
    /**
     * Validates if a string is a valid Hex color code.
     * Supports formats: #RRGGBB, RRGGBB, #RGB, RGB
     */
    fun isValidHexCode(hex: String): Boolean {
        val pattern = "^#?([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$".toRegex()
        return pattern.matches(hex)
    }

    /**
     * Normalizes hex code to uppercase and ensures it starts with #
     */
    fun normalizeHexCode(hex: String): String {
        if (hex.isBlank()) return ""
        val cleaned = if (hex.startsWith("#")) hex.substring(1) else hex
        return "#${cleaned.uppercase()}"
    }

    /**
     * Converts hex string to Compose Color.
     * Returns Color.Transparent if invalid.
     */
    fun parseColor(hex: String): Color {
        if (!isValidHexCode(hex)) return Color.Transparent
        
        return try {
            val cleaned = if (hex.startsWith("#")) hex.substring(1) else hex
            val fullHex = if (cleaned.length == 3) {
                // Expand RGB to RRGGBB
                cleaned.map { "$it$it" }.joinToString("")
            } else {
                cleaned
            }
            Color(android.graphics.Color.parseColor("#$fullHex"))
        } catch (e: Exception) {
            Color.Transparent
        }
    }
}
