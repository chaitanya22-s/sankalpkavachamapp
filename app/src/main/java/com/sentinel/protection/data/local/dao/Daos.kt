package com.sentinel.protection.data.local.dao

import androidx.room.*
import com.sentinel.protection.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SpamReportDao {
    @Query("SELECT * FROM spam_reports ORDER BY reportedAt DESC")
    fun getAllReports(): Flow<List<SpamReportEntity>>

    @Query("SELECT * FROM spam_reports WHERE phoneNumber = :number")
    suspend fun getReportsForNumber(number: String): List<SpamReportEntity>

    @Query("SELECT COUNT(*) FROM spam_reports WHERE phoneNumber = :number")
    suspend fun getReportCount(number: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(report: SpamReportEntity)

    @Delete
    suspend fun delete(report: SpamReportEntity)
}

@Dao
interface BlockedNumberDao {
    @Query("SELECT * FROM blocked_numbers ORDER BY blockedAt DESC")
    fun getAllBlocked(): Flow<List<BlockedNumberEntity>>

    @Query("SELECT COUNT(*) FROM blocked_numbers WHERE phoneNumber = :number")
    suspend fun isBlocked(number: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(blocked: BlockedNumberEntity)

    @Query("DELETE FROM blocked_numbers WHERE phoneNumber = :number")
    suspend fun unblock(number: String)
}

@Dao
interface SmsClassificationDao {
    @Query("SELECT * FROM sms_classifications ORDER BY classifiedAt DESC")
    fun getAll(): Flow<List<SmsClassificationEntity>>

    @Query("SELECT * FROM sms_classifications WHERE isSpam = 1 ORDER BY classifiedAt DESC")
    fun getSpam(): Flow<List<SmsClassificationEntity>>

    @Query("SELECT * FROM sms_classifications WHERE sender = :sender ORDER BY classifiedAt DESC LIMIT 10")
    suspend fun getForSender(sender: String): List<SmsClassificationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SmsClassificationEntity)

    @Query("SELECT COUNT(*) FROM sms_classifications")
    suspend fun getTotalCount(): Int

    @Query("SELECT COUNT(*) FROM sms_classifications WHERE isSpam = 1")
    suspend fun getSpamCount(): Int
}

@Dao
interface CallHistoryMetaDao {
    @Query("SELECT * FROM call_history_meta ORDER BY timestamp DESC LIMIT 100")
    fun getRecent(): Flow<List<CallHistoryMetaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CallHistoryMetaEntity)

    @Query("SELECT COUNT(*) FROM call_history_meta")
    suspend fun getTotalCount(): Int

    @Query("DELETE FROM call_history_meta WHERE timestamp < :beforeTime")
    suspend fun deleteOlderThan(beforeTime: Long)
}

@Dao
interface HoneyTrapFlagDao {
    @Query("SELECT * FROM honey_trap_flags WHERE isDismissed = 0 ORDER BY flaggedAt DESC")
    fun getActiveFlags(): Flow<List<HoneyTrapFlagEntity>>

    @Query("SELECT * FROM honey_trap_flags WHERE phoneNumber = :number ORDER BY flaggedAt DESC LIMIT 1")
    suspend fun getFlagForNumber(number: String): HoneyTrapFlagEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: HoneyTrapFlagEntity)

    @Query("UPDATE honey_trap_flags SET isDismissed = 1 WHERE phoneNumber = :number")
    suspend fun dismiss(number: String)

    @Query("SELECT COUNT(*) FROM honey_trap_flags WHERE isDismissed = 0")
    suspend fun getActiveCount(): Int
}

