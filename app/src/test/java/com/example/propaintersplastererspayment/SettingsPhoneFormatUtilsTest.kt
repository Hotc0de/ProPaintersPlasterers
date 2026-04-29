package com.example.propaintersplastererspayment

import com.example.propaintersplastererspayment.core.util.SettingsPhoneFormatUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsPhoneFormatUtilsTest {

    @Test
    fun formatInput_formatsNineDigitsAsThreeThreeThree() {
        val result = SettingsPhoneFormatUtils.formatInput("123456789")

        assertEquals("123-456-789", result)
    }

    @Test
    fun formatInput_formatsTenDigitsAsThreeThreeFour() {
        val result = SettingsPhoneFormatUtils.formatInput("1234567890")

        assertEquals("123-456-7890", result)
    }

    @Test
    fun isValid_returnsTrue_forNineDigitFormat() {
        assertTrue(SettingsPhoneFormatUtils.isValid("123-456-789"))
    }

    @Test
    fun isValid_returnsTrue_forTenDigitFormat() {
        assertTrue(SettingsPhoneFormatUtils.isValid("123-456-7890"))
    }

    @Test
    fun isValid_returnsFalse_forOtherLengths() {
        assertFalse(SettingsPhoneFormatUtils.isValid("123-4567"))
        assertFalse(SettingsPhoneFormatUtils.isValid("123-456-78901"))
    }
}

