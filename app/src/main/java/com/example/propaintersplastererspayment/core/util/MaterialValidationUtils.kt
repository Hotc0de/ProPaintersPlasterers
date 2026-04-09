package com.example.propaintersplastererspayment.core.util

object MaterialValidationUtils {
    fun parsePrice(priceText: String): Double? {
        return priceText.trim().replace(",", "").toDoubleOrNull()
    }

    fun validateMaterial(materialName: String, priceText: String): String? {
        return when {
            materialName.isBlank() -> "Material name is required."
            priceText.isBlank() -> "Price is required."
            parsePrice(priceText) == null -> "Enter a valid price."
            parsePrice(priceText)!! < 0 -> "Price cannot be negative."
            else -> null
        }
    }
}

