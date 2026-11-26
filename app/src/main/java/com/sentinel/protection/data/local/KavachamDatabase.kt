package com.sentinel.protection.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sentinel.protection.data.local.dao.*
import com.sentinel.protection.data.local.entity.*

@Database(
    entities = [
        SpamReportEntity::class,
        BlockedNumberEntity::class,
        SmsClassificationEntity::class,
        CallHistoryMetaEntity::class,
        HoneyTrapFlagEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class KavachamDatabase : RoomDatabase() {
    abstract fun spamReportDao(): SpamReportDao
    abstract fun blockedNumberDao(): BlockedNumberDao
    abstract fun smsClassificationDao(): SmsClassificationDao
    abstract fun callHistoryMetaDao(): CallHistoryMetaDao
    abstract fun honeyTrapFlagDao(): HoneyTrapFlagDao
}

