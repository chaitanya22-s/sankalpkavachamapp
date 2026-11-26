package com.sentinel.protection.ui.screens.assistant

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

data class ConversationMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@HiltViewModel
class AssistantViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _conversation = MutableStateFlow<List<ConversationMessage>>(emptyList())
    val conversation: StateFlow<List<ConversationMessage>> = _conversation.asStateFlow()

    private val _isSpeechAvailable = MutableStateFlow(SpeechRecognizer.isRecognitionAvailable(context))
    val isSpeechAvailable: StateFlow<Boolean> = _isSpeechAvailable.asStateFlow()

    private val _isTtsReady = MutableStateFlow(false)
    val isTtsReady: StateFlow<Boolean> = _isTtsReady.asStateFlow()

    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null

    init {
        initTts()
        addGreeting()
    }

    private fun addGreeting() {
        addAssistantMessage(
            "నమస్కారం! నేను కవచం అమ్మ. నేను మీకు సైబర్ భద్రత విషయాలలో సహాయం చేయగలను.\n" +
            "Hello! I am Kavacham Amma. I can help you with cyber safety.\n\n" +
            "మీరు ఏమి అడగవచ్చు:\n• Number spam aa? (Is this number spam?)\n• OTP cheppali aa? (Should I share OTP?)\n• 1930 ki ela call cheyyali? (How to call 1930?)"
        )
    }

    private fun initTts() {
        tts = TextToSpeech(context) { status ->
            _isTtsReady.value = status == TextToSpeech.SUCCESS
            if (status == TextToSpeech.SUCCESS) {
                // Try Telugu first, fall back to English
                val teluguResult = tts?.setLanguage(Locale("te", "IN"))
                if (teluguResult == TextToSpeech.LANG_NOT_SUPPORTED ||
                    teluguResult == TextToSpeech.LANG_MISSING_DATA) {
                    tts?.language = Locale.ENGLISH
                }
            }
        }
    }

    fun startListening() {
        if (!_isSpeechAvailable.value) return
        _isListening.value = true
        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onResults(results: Bundle?) {
                    _isListening.value = false
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val recognized = matches?.firstOrNull() ?: return
                    processUserInput(recognized)
                }
                override fun onError(error: Int) {
                    _isListening.value = false
                    addAssistantMessage("I couldn't hear you clearly. Please try again. / వినలేకపోయాను. మళ్ళీ ప్రయత్నించండి.")
                }
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() { _isListening.value = false }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "te-IN")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "te-IN,en-IN")
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        _isListening.value = false
    }

    fun processUserInput(text: String) {
        addUserMessage(text)
        _isProcessing.value = true
        viewModelScope.launch {
            val response = generateResponse(text.lowercase())
            _isProcessing.value = false
            addAssistantMessage(response)
            speak(response)
        }
    }

    private fun generateResponse(input: String): String {
        return when {
            // OTP related
            input.contains("otp") -> {
                "OTP evariki cheppakandi – bank employees kuda OTP adagakoodadu!\n" +
                "⚠️ Never share your OTP with anyone – not even bank employees!"
            }
            // Number spam check
            input.contains("spam") && (input.contains("number") || input.contains("numb")) -> {
                "Number check cheyyadaniki Dialer screen ki velli number type cheyandi, tarvata 'Check' button press cheyandi.\n" +
                "Go to the Dialer tab, type the number, and press 'Check' to look up local reports."
            }
            // Money transfer
            input.contains("dabbu") || input.contains("money") || input.contains("transfer") || input.contains("pampandi") -> {
                "⚠️ SCAM ALERT: Dabbu pampakandi! Online fraud lo ee type request chala common.\n" +
                "Do NOT send money to unknown people. This is a common fraud pattern!"
            }
            // 1930
            input.contains("1930") -> {
                "1930 Indian cyber crime helpline number. Emergency lo call cheyandi.\n" +
                "1930 is India's Cyber Crime helpline. Call immediately if you are a victim of online fraud."
            }
            // Photo sharing
            input.contains("photo") || input.contains("pic") || input.contains("image") -> {
                "⚠️ Private photos evariki pampakandi! Honey trap scam avvadam chance undi.\n" +
                "NEVER share private photos with unknown people. This is a classic honey trap scam."
            }
            // Bank account
            input.contains("bank") || input.contains("account") -> {
                "Bank account details – account number, IFSC, card number – evariki cheppakandi.\n" +
                "Never share your bank account details, card number, or IFSC with anyone."
            }
            // Deepfake / video
            input.contains("deepfake") || input.contains("fake video") -> {
                "Media Checker feature ni upayoginchadam ki, bottom menu lo Media option ni tap cheyandi.\n" +
                "Use the Media Authenticity Checker in the Quick Actions to analyze suspicious photos or videos."
            }
            // Hello/greeting
            input.contains("hello") || input.contains("hi") || input.contains("namaskaram") -> {
                "Namaskaram! Nenu meeru cyber safety questions ki sahaayapadanu.\n" +
                "Hello! I'm here to help you with cyber safety questions."
            }
            // Cybercrime complaint
            input.contains("complaint") || input.contains("report") || input.contains("fraud") -> {
                "Cyber fraud ki target ayite:\n1. 1930 ki call cheyandi\n2. cybercrime.gov.in website visit cheyandi\n\n" +
                "If you are a fraud victim: 1. Call 1930  2. Visit cybercrime.gov.in to file a complaint."
            }
            // Generic fallback
            else -> {
                "Meeru idi artham chesukogaliganu. Meeru ela help cheyagalanu?\n" +
                "Common questions:\n• Is [number] spam?\n• Should I share my OTP?\n• How to call 1930?\n• What is a honey trap?"
            }
        }
    }

    private fun speak(text: String) {
        if (_isTtsReady.value) {
            // Speak only English parts for better clarity when Telugu TTS may not be available
            val englishLines = text.lines().filter { line ->
                line.none { c -> c.code in 0x0C00..0x0C7F } // Filter out Telugu Unicode range
            }.joinToString(" ")
            tts?.speak(englishLines, TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString())
        }
    }

    private fun addUserMessage(text: String) {
        _conversation.value = _conversation.value + ConversationMessage(text, isUser = true)
    }

    private fun addAssistantMessage(text: String) {
        _conversation.value = _conversation.value + ConversationMessage(text, isUser = false)
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
        speechRecognizer?.destroy()
    }
}

