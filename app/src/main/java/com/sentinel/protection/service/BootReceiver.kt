package com.sentinel.protection.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import timber.log.Timber

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Timber.d("Boot completed – Sankalp Kavacham protection services started")
            // WorkManager jobs are automatically rescheduled after boot
            // No additional work needed here for now
        }
    }
}

