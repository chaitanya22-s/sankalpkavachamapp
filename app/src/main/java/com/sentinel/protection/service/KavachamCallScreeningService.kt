package com.sentinel.protection.service

import android.telecom.Call
import android.telecom.CallScreeningService
import com.sentinel.protection.data.local.dao.BlockedNumberDao
import com.sentinel.protection.data.local.dao.SpamReportDao
import com.sentinel.protection.util.PhoneNumberHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Android CallScreeningService.
 *
 * Platform limitations:
 * - Can screen incoming calls and suggest responses to the telecom framework.
 * - Cannot listen to or record call audio for normal cellular calls.
 * - Works only when the app is set as the default call screening app via
 *   android.telecom.TelecomManager.requestRoleAsync(ROLE_CALL_SCREENING).
 */
@AndroidEntryPoint
class KavachamCallScreeningService : CallScreeningService() {

    @Inject lateinit var blockedNumberDao: BlockedNumberDao
    @Inject lateinit var spamReportDao: SpamReportDao

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onScreenCall(callDetails: Call.Details) {
        val rawNumber = callDetails.handle?.schemeSpecificPart ?: run {
            respondToCall(callDetails, buildAllowResponse())
            return
        }

        val normalized = PhoneNumberHelper.normalizeForLookup(rawNumber)

        serviceScope.launch {
            try {
                val isBlocked = blockedNumberDao.isBlocked(normalized) > 0
                val reportCount = spamReportDao.getReportCount(normalized)

                val response = when {
                    isBlocked -> {
                        Timber.d("Blocking call from $normalized – in block list")
                        CallResponse.Builder()
                            .setDisallowCall(true)
                            .setRejectCall(true)
                            .setSkipCallLog(false)
                            .setSkipNotification(false)
                            .build()
                    }
                    reportCount >= 5 -> {
                        // Silently reject highly reported numbers but log the call
                        Timber.d("Silencing call from $normalized – $reportCount reports")
                        CallResponse.Builder()
                            .setDisallowCall(false)
                            .setSilenceCall(true)
                            .setSkipCallLog(false)
                            .build()
                    }
                    reportCount >= 2 -> {
                        // Allow but show notification
                        Timber.d("Allowing suspicious call from $normalized – $reportCount reports")
                        buildAllowResponse()
                    }
                    else -> buildAllowResponse()
                }
                respondToCall(callDetails, response)
            } catch (e: Exception) {
                Timber.e(e, "Error in call screening")
                respondToCall(callDetails, buildAllowResponse())
            }
        }
    }

    private fun buildAllowResponse() = CallResponse.Builder()
        .setDisallowCall(false)
        .setRejectCall(false)
        .setSkipCallLog(false)
        .setSkipNotification(false)
        .build()
}

