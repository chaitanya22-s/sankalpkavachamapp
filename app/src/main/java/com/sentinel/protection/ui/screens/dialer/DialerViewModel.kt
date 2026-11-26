package com.sentinel.protection.ui.screens.dialer

import android.content.Context
import android.provider.CallLog
import android.provider.ContactsContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sentinel.protection.data.local.dao.BlockedNumberDao
import com.sentinel.protection.data.local.dao.SpamReportDao
import com.sentinel.protection.data.repository.SupabaseSpamRepository
import com.sentinel.protection.domain.model.*
import com.sentinel.protection.util.PhoneNumberHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DialerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val spamReportDao: SpamReportDao,
    private val blockedNumberDao: BlockedNumberDao,
    private val supabaseSpamRepository: SupabaseSpamRepository
) : ViewModel() {

    private val _dialedNumber = MutableStateFlow("")
    val dialedNumber: StateFlow<String> = _dialedNumber.asStateFlow()

    private val _numberCheckResult = MutableStateFlow<NumberCheckResult?>(null)
    val numberCheckResult: StateFlow<NumberCheckResult?> = _numberCheckResult.asStateFlow()

    private val _recentCalls = MutableStateFlow<List<CallRecord>>(emptyList())
    val recentCalls: StateFlow<List<CallRecord>> = _recentCalls.asStateFlow()

    private val _contacts = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val contacts: StateFlow<List<Pair<String, String>>> = _contacts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun appendDigit(digit: String) {
        _dialedNumber.value = _dialedNumber.value + digit
    }

    fun backspace() {
        val current = _dialedNumber.value
        if (current.isNotEmpty()) {
            _dialedNumber.value = current.dropLast(1)
        }
    }

    fun clearNumber() {
        _dialedNumber.value = ""
        _numberCheckResult.value = null
    }

    fun setNumber(number: String) {
        _dialedNumber.value = number
    }

    fun checkNumber(number: String = _dialedNumber.value) {
        if (number.isBlank()) return
        viewModelScope.launch {
            _isLoading.value = true
            _numberCheckResult.value = null
            try {
                val parsed = PhoneNumberHelper.parse(number, "IN")
                if (!parsed.isValid) {
                    _numberCheckResult.value = NumberCheckResult(
                        phoneNumber = number,
                        formattedNumber = number,
                        isValid = false,
                        explanation = "Invalid phone number format"
                    )
                    return@launch
                }

                val normalized = parsed.formattedE164
                val contactName = lookupContact(normalized) ?: lookupContact(number)
                val localReportCount = spamReportDao.getReportCount(normalized)
                val isBlocked = blockedNumberDao.isBlocked(normalized) > 0

                // Query Supabase backend for community reputation
                val supabaseResult = supabaseSpamRepository.checkNumberReputation(normalized).getOrNull()
                val communityCount = supabaseResult?.reportedCount ?: 0
                val isVerifiedCommunitySpam = supabaseResult?.isVerifiedSpam == true || communityCount >= 10

                val totalReportCount = maxOf(localReportCount, communityCount)

                val riskLevel = when {
                    isBlocked || isVerifiedCommunitySpam -> RiskLevel.RED
                    totalReportCount >= 5 -> RiskLevel.RED
                    totalReportCount >= 2 -> RiskLevel.ORANGE
                    else -> RiskLevel.GREEN
                }

                val explanation = when {
                    isVerifiedCommunitySpam ->
                        "🚨 Community Verified Spam ($communityCount reports on Supabase): ${supabaseResult?.reason}"
                    isBlocked ->
                        "This number is in your block list."
                    riskLevel == RiskLevel.RED ->
                        "High risk – reported by $totalReportCount users across the community."
                    riskLevel == RiskLevel.ORANGE ->
                        "Suspicious – reported by $totalReportCount user(s)."
                    contactName != null ->
                        "Known contact."
                    else ->
                        "No spam reports found in local or community database."
                }

                _numberCheckResult.value = NumberCheckResult(
                    phoneNumber = number,
                    formattedNumber = parsed.formattedInternational,
                    isValid = true,
                    contactName = contactName,
                    isInContacts = contactName != null,
                    riskLevel = riskLevel,
                    reportCount = totalReportCount,
                    region = parsed.regionCode,
                    explanation = explanation
                )
            } catch (e: Exception) {
                Timber.e(e, "Number check error")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Report a number to Supabase and local DB
     */
    fun reportNumber(number: String, reason: String, type: String = "Spam Call") {
        viewModelScope.launch {
            val normalized = PhoneNumberHelper.normalizeForLookup(number)
            supabaseSpamRepository.reportNumber(
                fraudNumber = normalized,
                reason = reason,
                type = type
            )
            // Re-check
            checkNumber(number)
        }
    }

    fun loadRecentCalls(hasPermission: Boolean) {
        if (!hasPermission) return
        viewModelScope.launch {
            _recentCalls.value = fetchCallLog()
        }
    }

    fun loadContacts(hasPermission: Boolean) {
        if (!hasPermission) return
        viewModelScope.launch {
            _contacts.value = fetchContacts()
        }
    }

    private suspend fun lookupContact(number: String): String? = withContext(Dispatchers.IO) {
        try {
            val uri = ContactsContract.PhoneLookup.CONTENT_FILTER_URI.buildUpon()
                .appendPath(number).build()
            context.contentResolver.query(
                uri,
                arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME),
                null, null, null
            )?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            }
        } catch (e: SecurityException) { null }
    }

    private suspend fun fetchCallLog(): List<CallRecord> = withContext(Dispatchers.IO) {
        val calls = mutableListOf<CallRecord>()
        try {
            val projection = arrayOf(
                CallLog.Calls.NUMBER,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.TYPE,
                CallLog.Calls.DURATION,
                CallLog.Calls.DATE
            )
            context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                projection,
                null, null,
                "${CallLog.Calls.DATE} DESC LIMIT 100"
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val number = cursor.getString(0) ?: continue
                    val name = cursor.getString(1)
                    val typeInt = cursor.getInt(2)
                    val duration = cursor.getLong(3)
                    val date = cursor.getLong(4)
                    val type = when (typeInt) {
                        CallLog.Calls.INCOMING_TYPE -> CallType.INCOMING
                        CallLog.Calls.OUTGOING_TYPE -> CallType.OUTGOING
                        CallLog.Calls.MISSED_TYPE -> CallType.MISSED
                        CallLog.Calls.REJECTED_TYPE -> CallType.REJECTED
                        CallLog.Calls.BLOCKED_TYPE -> CallType.BLOCKED
                        else -> CallType.INCOMING
                    }
                    calls.add(CallRecord(
                        phoneNumber = number,
                        contactName = if (name.isNullOrBlank()) null else name,
                        type = type,
                        durationSeconds = duration,
                        timestamp = date
                    ))
                }
            }
        } catch (e: SecurityException) {
            Timber.w("Call log permission denied")
        }
        calls
    }

    private suspend fun fetchContacts(): List<Pair<String, String>> = withContext(Dispatchers.IO) {
        val result = mutableListOf<Pair<String, String>>()
        try {
            context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ),
                null, null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val name = cursor.getString(0) ?: continue
                    val number = cursor.getString(1) ?: continue
                    result.add(Pair(name, number))
                }
            }
        } catch (e: SecurityException) {
            Timber.w("Contacts permission denied")
        }
        result
    }
}
