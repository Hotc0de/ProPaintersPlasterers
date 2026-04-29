package com.example.propaintersplastererspayment.core.util

data class BusinessPhoneCountryCode(
    val isoCode: String,
    val dialCode: String,
    val label: String
)

object BusinessPhoneCountryCodes {
    val default = BusinessPhoneCountryCode(
        isoCode = "NZ",
        dialCode = "+64",
        label = "NZ +64"
    )

    val options = listOf(
        default,
        BusinessPhoneCountryCode("AU", "+61", "AU +61"),
        BusinessPhoneCountryCode("US", "+1", "US +1"),
        BusinessPhoneCountryCode("CA", "+1", "CA +1"),
        BusinessPhoneCountryCode("GB", "+44", "UK +44")
    )

    fun findByIsoOrDefault(isoCode: String): BusinessPhoneCountryCode =
        options.firstOrNull { it.isoCode == isoCode } ?: default

    fun findByDialOrDefault(dialCode: String): BusinessPhoneCountryCode =
        options.firstOrNull { it.dialCode == dialCode } ?: default
}

