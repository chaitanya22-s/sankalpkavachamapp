package com.sentinel.protection.ui.screens.spam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sentinel.protection.data.local.dao.SmsClassificationDao
import com.sentinel.protection.data.local.entity.SmsClassificationEntity
import com.sentinel.protection.data.remote.model.SpamNumberDto
import com.sentinel.protection.data.repository.SupabaseSpamRepository
import com.sentinel.protection.domain.model.HoneyTrapAnalysis
import com.sentinel.protection.domain.model.RiskLevel
import com.sentinel.protection.util.HoneyTrapDetector
import com.sentinel.protection.util.ScamKeywordDetector
import com.sentinel.protection.util.SmsSpamDetector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpamViewModel @Inject constructor(
    private val smsClassificationDao: SmsClassificationDao,
    private val supabaseSpamRepository: SupabaseSpamRepository
) : ViewModel() {

    val spamMessages: StateFlow<List<SmsClassificationEntity>> = smsClassificationDao
        .getSpam()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Community Reported Spam Numbers from Supabase
    private val _communitySpamNumbers = MutableStateFlow<List<SpamNumberDto>>(emptyList())
    val communitySpamNumbers: StateFlow<List<SpamNumberDto>> = _communitySpamNumbers.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    // Message scan state
    private val _scanInput = MutableStateFlow("")
    val scanInput: StateFlow<String> = _scanInput.asStateFlow()

    private val _spamAnalysisResult = MutableStateFlow<SmsSpamDetector.SpamAnalysis?>(null)
    val spamAnalysisResult: StateFlow<SmsSpamDetector.SpamAnalysis?> = _spamAnalysisResult.asStateFlow()

    private val _scamDetectionResult = MutableStateFlow<ScamKeywordDetector.ScamDetectionResult?>(null)
    val scamDetectionResult: StateFlow<ScamKeywordDetector.ScamDetectionResult?> = _scamDetectionResult.asStateFlow()

    private val _honeyTrapResult = MutableStateFlow<HoneyTrapAnalysis?>(null)
    val honeyTrapResult: StateFlow<HoneyTrapAnalysis?> = _honeyTrapResult.asStateFlow()

    init {
        fetchCommunitySpam()
    }

    fun fetchCommunitySpam() {
        viewModelScope.launch {
            _isSyncing.value = true
            supabaseSpamRepository.fetchCommunitySpamNumbers()
                .onSuccess { numbers ->
                    _communitySpamNumbers.value = numbers
                }
            _isSyncing.value = false
        }
    }

    fun setScanInput(text: String) {
        _scanInput.value = text
    }

    fun analyzeMessage(sender: String = "Unknown", body: String = _scanInput.value) {
        if (body.isBlank()) return
        viewModelScope.launch {
            val analysis = SmsSpamDetector.analyze(sender, body)
            val scamResult = ScamKeywordDetector.analyze(body)
            val honeyTrapResult = HoneyTrapDetector.analyzeMessage(body)

            _spamAnalysisResult.value = analysis
            _scamDetectionResult.value = scamResult
            _honeyTrapResult.value = honeyTrapResult

            // If message is high risk spam, optionally report to Supabase spam_messages
            if (analysis.isSpam) {
                val type = when {
                    honeyTrapResult.riskLevel != RiskLevel.GREEN -> "Honey Trap"
                    analysis.category == SmsSpamDetector.ScamCategory.TRAFFIC_CHALLAN_APK -> "Fake Suffix"
                    else -> "Phishing"
                }
                supabaseSpamRepository.reportSpamMessage(
                    sender = sender,
                    message = body,
                    type = type,
                    isSpam = true
                )
            }
        }
    }

    fun reportNumberToCommunity(number: String, reason: String, type: String = "Spam Call") {
        viewModelScope.launch {
            supabaseSpamRepository.reportNumber(
                fraudNumber = number,
                reason = reason,
                type = type
            )
            fetchCommunitySpam()
        }
    }

    fun clearScanResults() {
        _scanInput.value = ""
        _spamAnalysisResult.value = null
        _scamDetectionResult.value = null
        _honeyTrapResult.value = null
    }
}
