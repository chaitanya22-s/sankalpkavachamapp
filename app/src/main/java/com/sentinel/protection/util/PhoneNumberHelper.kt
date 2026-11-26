package com.sentinel.protection.util

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber

object PhoneNumberHelper {
    private val phoneUtil = PhoneNumberUtil.getInstance()

    data class ParseResult(
        val isValid: Boolean,
        val formattedE164: String,
        val formattedNational: String,
        val formattedInternational: String,
        val regionCode: String?,
        val numberType: String,
        val originalInput: String
    )

    fun parse(input: String, defaultRegion: String = "IN"): ParseResult {
        val cleaned = input.trim().replace(" ", "").replace("-", "").replace("(", "").replace(")", "")
        return try {
            val proto: Phonenumber.PhoneNumber = phoneUtil.parse(cleaned, defaultRegion)
            val isValid = phoneUtil.isValidNumber(proto)
            ParseResult(
                isValid = isValid,
                formattedE164 = if (isValid) phoneUtil.format(proto, PhoneNumberUtil.PhoneNumberFormat.E164) else cleaned,
                formattedNational = if (isValid) phoneUtil.format(proto, PhoneNumberUtil.PhoneNumberFormat.NATIONAL) else cleaned,
                formattedInternational = if (isValid) phoneUtil.format(proto, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL) else cleaned,
                regionCode = if (isValid) phoneUtil.getRegionCodeForNumber(proto) else null,
                numberType = if (isValid) phoneUtil.getNumberType(proto).name else "UNKNOWN",
                originalInput = input
            )
        } catch (e: NumberParseException) {
            ParseResult(
                isValid = false,
                formattedE164 = cleaned,
                formattedNational = cleaned,
                formattedInternational = cleaned,
                regionCode = null,
                numberType = "UNKNOWN",
                originalInput = input
            )
        }
    }

    fun isValidIndianNumber(input: String): Boolean {
        val result = parse(input, "IN")
        return result.isValid && result.regionCode == "IN"
    }

    fun normalizeForLookup(input: String, defaultRegion: String = "IN"): String {
        val result = parse(input, defaultRegion)
        return if (result.isValid) result.formattedE164 else input.trim()
    }
}

