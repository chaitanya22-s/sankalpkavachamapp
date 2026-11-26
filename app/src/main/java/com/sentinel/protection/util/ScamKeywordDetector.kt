package com.sentinel.protection.util

import com.sentinel.protection.domain.model.RiskLevel

object ScamKeywordDetector {

    // Keywords in English
    private val SCAM_KEYWORDS_EN = listOf(
        "otp", "send money", "bank account", "share password", "kyc",
        "remote access", "send private photo", "transfer funds", "verify account",
        "gift card", "bitcoin", "crypto investment", "aadhar number", "pan number"
    )

    // Keywords in Telugu
    private val SCAM_KEYWORDS_TE = listOf(
        "otp cheppandi", "dabbu pampandi", "bank account details ivvandi",
        "photo pampandi", "password cheppandi", "otp share cheyyi",
        "nee account suspend avutundi", "kyc update cheyyi", "money pampandi"
    )

    data class ScamDetectionResult(
        val hasScamIndicators: Boolean,
        val matchedKeywords: List<String>,
        val riskLevel: RiskLevel,
        val warningMessage: String
    )

    fun analyze(text: String): ScamDetectionResult {
        val textLower = text.lowercase()
        val matched = mutableListOf<String>()

        matched.addAll(SCAM_KEYWORDS_EN.filter { textLower.contains(it) })
        matched.addAll(SCAM_KEYWORDS_TE.filter { textLower.contains(it) })

        val riskLevel = when {
            matched.size >= 3 -> RiskLevel.RED
            matched.size >= 1 -> RiskLevel.ORANGE
            else -> RiskLevel.GREEN
        }

        val warning = if (matched.isNotEmpty()) {
            "⚠️ SCAM WARNING – Do not share OTP or send money. మీ OTP ఎవరికీ చెప్పకండి."
        } else ""

        return ScamDetectionResult(
            hasScamIndicators = matched.isNotEmpty(),
            matchedKeywords = matched,
            riskLevel = riskLevel,
            warningMessage = warning
        )
    }
}

