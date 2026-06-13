package com.example.propaintersplastererspayment.core.util

import java.util.Locale

object AddressKeyUtils {
    fun normalize(address: String): String =
        address.trim()
            .lowercase(Locale.ROOT)
            .replace(Regex("\\s+"), " ")
}
