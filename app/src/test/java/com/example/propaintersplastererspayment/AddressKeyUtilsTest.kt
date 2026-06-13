package com.example.propaintersplastererspayment

import com.example.propaintersplastererspayment.core.util.AddressKeyUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class AddressKeyUtilsTest {

    @Test
    fun normalize_ignoresCaseAndRepeatedWhitespace() {
        assertEquals(
            "123 ttt unit 2",
            AddressKeyUtils.normalize("  123   TTT\nUnit 2  ")
        )
    }
}
