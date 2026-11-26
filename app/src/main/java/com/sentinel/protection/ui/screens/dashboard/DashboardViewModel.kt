package com.sentinel.protection.ui.screens.dashboard

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sentinel.protection.data.local.dao.CallHistoryMetaDao
import com.sentinel.protection.data.local.dao.HoneyTrapFlagDao
import com.sentinel.protection.data.local.dao.SmsClassificationDao
import com.sentinel.protection.data.local.dao.SpamReportDao
import com.sentinel.protection.domain.model.ProtectionStats
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val callHistoryMetaDao: CallHistoryMetaDao,
    private val smsClassificationDao: SmsClassificationDao,
    private val spamReportDao: SpamReportDao,
    private val honeyTrapFlagDao: HoneyTrapFlagDao
) : ViewModel() {

    private val _stats = MutableStateFlow(ProtectionStats())
    val stats: StateFlow<ProtectionStats> = _stats.asStateFlow()

    private val _permissionsStatus = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val permissionsStatus: StateFlow<Map<String, Boolean>> = _permissionsStatus.asStateFlow()

    init {
        loadStats()
        checkPermissions()
    }

    private fun loadStats() {
        viewModelScope.launch {
            val callsScreened = callHistoryMetaDao.getTotalCount()
            val messagesChecked = smsClassificationDao.getTotalCount()
            val spamMessages = smsClassificationDao.getSpamCount()
            val honeyTrapThreats = honeyTrapFlagDao.getActiveCount()
            val threatsDetected = spamMessages + honeyTrapThreats

            _stats.value = ProtectionStats(
                callsScreened = callsScreened,
                messagesChecked = messagesChecked,
                threatsDetected = threatsDetected,
                lastScanTime = System.currentTimeMillis(),
                isCallScreeningActive = hasPermission(Manifest.permission.READ_PHONE_STATE),
                isSmsFilteringActive = hasPermission(Manifest.permission.READ_SMS)
            )
        }
    }

    fun refreshStats() {
        loadStats()
        checkPermissions()
    }

    private fun checkPermissions() {
        val permissions = mapOf(
            "Contacts" to Manifest.permission.READ_CONTACTS,
            "Phone" to Manifest.permission.READ_PHONE_STATE,
            "Call Log" to Manifest.permission.READ_CALL_LOG,
            "SMS" to Manifest.permission.READ_SMS,
            "Microphone" to Manifest.permission.RECORD_AUDIO,
            "Camera" to Manifest.permission.CAMERA
        )
        _permissionsStatus.value = permissions.mapValues { (_, perm) -> hasPermission(perm) }
    }

    private fun hasPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}

