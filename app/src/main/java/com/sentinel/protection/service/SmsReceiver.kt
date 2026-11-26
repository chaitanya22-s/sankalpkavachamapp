package com.sentinel.protection.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.sentinel.protection.data.local.dao.SmsClassificationDao
import com.sentinel.protection.data.local.entity.SmsClassificationEntity
import com.sentinel.protection.util.SmsSpamDetector
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * SMS Receiver.
 *
 * Platform note:
 * - Reading SMS content in a broadcast receiver works when the app has READ_SMS permission.
 * - Moving messages to spam requires being the default SMS app.
 * - This receiver classifies incoming SMS and stores the result in the local DB.
 */
@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    @Inject lateinit var smsClassificationDao: SmsClassificationDao

    private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        for (sms in messages) {
            val sender = sms.originatingAddress ?: continue
            val body = sms.messageBody ?: continue

            receiverScope.launch {
                try {
                    val analysis = SmsSpamDetector.analyze(sender, body)
                    val hash = "${sender}_${body.hashCode()}"
                    smsClassificationDao.insert(
                        SmsClassificationEntity(
                            sender = sender,
                            messageHash = hash,
                            isSpam = analysis.isSpam,
                            spamReason = if (analysis.reasons.isNotEmpty()) analysis.reasons.first() else null,
                            riskLevel = analysis.riskLevel,
                            classifiedAt = System.currentTimeMillis()
                        )
                    )
                    Timber.d("SMS from $sender classified: ${analysis.riskLevel}")
                } catch (e: Exception) {
                    Timber.e(e, "SMS classification error")
                }
            }
        }
    }
}

