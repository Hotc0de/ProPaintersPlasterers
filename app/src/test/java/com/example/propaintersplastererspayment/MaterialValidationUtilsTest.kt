package com.example.propaintersplastererspayment

import com.example.propaintersplastererspayment.core.util.MaterialValidationUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MaterialValidationUtilsTest {

    @Test
    fun parsePrice_returnsDouble_whenTextIsValid() {
        val result = MaterialValidationUtils.parsePrice("125.50")

        assertEquals(125.50, result ?: 0.0, 0.0001)
    }

    @Test
    fun validateMaterial_returnsMessage_whenNameIsBlank() {
        val result = MaterialValidationUtils.validateMaterial(
            materialName = "",
            priceText = "125.50"
        )

        assertEquals("Material name is required.", result)
    }

    @Test
    fun validateMaterial_returnsNull_whenFieldsAreValid() {
        val result = MaterialValidationUtils.validateMaterial(
            materialName = "Paint",
            priceText = "125.50"
        )

        assertNull(result)
    }
}

