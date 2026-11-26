package com.sentinel.protection.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SpamNumberDto(
    @SerialName("id") val id: Long? = null,
    @SerialName("number") val number: String,
    @SerialName("reason") val reason: String,
    @SerialName("reported_count") val reportedCount: Int = 1,
    @SerialName("is_verified_spam") val isVerifiedSpam: Boolean = false,
    @SerialName("risk_level") val riskLevel: String = "YELLOW",
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class SpamMessageDto(
    @SerialName("id") val id: Long? = null,
    @SerialName("sender") val sender: String,
    @SerialName("message") val message: String,
    @SerialName("type") val type: String, // Fake Suffix / Honey Trap / Deepfake / Bomber / Phishing
    @SerialName("is_spam") val isSpam: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class ReportDto(
    @SerialName("id") val id: Long? = null,
    @SerialName("user_number") val userNumber: String? = "Anonymous",
    @SerialName("fraud_number") val fraudNumber: String,
    @SerialName("message") val message: String? = "",
    @SerialName("type") val type: String = "Spam Call",
    @SerialName("created_at") val createdAt: String? = null
)
