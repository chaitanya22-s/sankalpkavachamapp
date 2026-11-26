package com.sentinel.protection.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spam_reports")
data class SpamReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phoneNumber: String,
    val category: String,
    val notes: String = "",
    val reportedAt: Long = System.currentTimeMillis(),
    val reportedByUser: Boolean = true
)

@Entity(tableName = "blocked_numbers")
data class BlockedNumberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phoneNumber: String,
    val reason: String,
    val blockedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sms_classifications")
data class SmsClassificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String,
    val messageHash: String,
    val isSpam: Boolean,
    val spamReason: String? = null,
    val riskLevel: String,
    val classifiedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "call_history_meta")
data class CallHistoryMetaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phoneNumber: String,
    val contactName: String? = null,
    val callType: String,
    val durationSeconds: Long = 0,
    val timestamp: Long,
    val isSpam: Boolean = false,
    val riskLevel: String = "UNKNOWN"
)

@Entity(tableName = "honey_trap_flags")
data class HoneyTrapFlagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phoneNumber: String,
    val riskScore: Int,
    val triggers: String, // JSON
    val flaggedAt: Long = System.currentTimeMillis(),
    val isDismissed: Boolean = false
)

