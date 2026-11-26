package com.sentinel.protection.di

import android.content.Context
import androidx.room.Room
import com.sentinel.protection.data.local.KavachamDatabase
import com.sentinel.protection.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): KavachamDatabase {
        return Room.databaseBuilder(
            context,
            KavachamDatabase::class.java,
            "kavacham_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides fun provideSpamReportDao(db: KavachamDatabase): SpamReportDao = db.spamReportDao()
    @Provides fun provideBlockedNumberDao(db: KavachamDatabase): BlockedNumberDao = db.blockedNumberDao()
    @Provides fun provideSmsClassificationDao(db: KavachamDatabase): SmsClassificationDao = db.smsClassificationDao()
    @Provides fun provideCallHistoryMetaDao(db: KavachamDatabase): CallHistoryMetaDao = db.callHistoryMetaDao()
    @Provides fun provideHoneyTrapFlagDao(db: KavachamDatabase): HoneyTrapFlagDao = db.honeyTrapFlagDao()
}

