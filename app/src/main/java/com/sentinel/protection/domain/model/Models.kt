package com.sentinel.protection.domain.model

import kotlinx.serialization.Serializable

enum class RiskLevel { GREEN, ORANGE, RED, UNKNOWN }

@Serializable
data class NumberCheckResult(
    val phoneNumber: String,
    val formattedNumber: String,
    val isValid: Boolean,
    val contactName: String? = null,
    val isInContacts: Boolean = false,
    val riskLevel: RiskLevel = RiskLevel.UNKNOWN,
    val reportCount: Int = 0,
    val spamCategories: List<String> = emptyList(),
    val explanation: String = "",
    val region: String? = null,
    val carrier: String? = null
)

data class CallRecord(
    val id: Long = 0,
    val phoneNumber: String,
    val contactName: String? = null,
    val type: CallType,
    val durationSeconds: Long = 0,
    val timestamp: Long,
    val isSpam: Boolean = false,
    val riskLevel: RiskLevel = RiskLevel.UNKNOWN
)

enum class CallType { INCOMING, OUTGOING, MISSED, REJECTED, BLOCKED }

data class SmsMessage(
    val id: Long = 0,
    val sender: String,
    val body: String,
    val timestamp: Long,
    val isSpam: Boolean = false,
    val spamReason: String? = null,
    val suspiciousLinks: List<String> = emptyList(),
    val riskLevel: RiskLevel = RiskLevel.UNKNOWN
)

data class BlockedNumber(
    val id: Long = 0,
    val phoneNumber: String,
    val reason: String,
    val blockedAt: Long
)

data class UserReport(
    val id: Long = 0,
    val phoneNumber: String,
    val category: ReportCategory,
    val notes: String = "",
    val reportedAt: Long
)

enum class ReportCategory {
    SPAM_CALL, FRAUD, HONEY_TRAP, ROBOCALL, HARASSMENT, OTHER
}

data class HoneyTrapAnalysis(
    val riskLevel: RiskLevel,
    val riskScore: Int, // 0-100
    val triggers: List<HoneyTrapTrigger>,
    val explanation: String
)

data class HoneyTrapTrigger(
    val type: TriggerType,
    val description: String,
    val matchedText: String
)

enum class TriggerType {
    UNKNOWN_CONTACT,
    ROMANTIC_ESCALATION,
    PHOTO_REQUEST,
    MONEY_REQUEST,
    THREAT_BLACKMAIL,
    VIDEO_CALL_INVITE,
    FINANCIAL_EXTORTION
}

data class MediaAnalysisResult(
    val mediaType: MediaType,
    val analysisAvailable: Boolean,
    val overallResult: MediaAuthenticityResult,
    val indicators: List<String>,
    val suspiciousIndicators: List<String>,
    val explanation: String,
    val limitations: String
)

enum class MediaType { PHOTO, VIDEO, AUDIO }
enum class MediaAuthenticityResult { LIKELY_AUTHENTIC, SUSPICIOUS, ANALYSIS_UNAVAILABLE }

data class ProtectionStats(
    val callsScreened: Int = 0,
    val messagesChecked: Int = 0,
    val threatsDetected: Int = 0,
    val lastScanTime: Long? = null,
    val isCallScreeningActive: Boolean = false,
    val isSmsFilteringActive: Boolean = false
)

