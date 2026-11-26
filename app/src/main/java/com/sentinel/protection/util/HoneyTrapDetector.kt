package com.sentinel.protection.util

import com.sentinel.protection.domain.model.*

object HoneyTrapDetector {

    // Romantic phrases in English and Telugu (script and romanized)
    private val ROMANTIC_ESCALATION_PHRASES = listOf(
        "hi handsome", "hi beautiful", "i like you", "i love you", "you are cute",
        "you are handsome", "i miss you", "let's be friends", "i fell for you",
        "you are amazing", "nenu ninu istapaddanu", "chala bagunnavu", "friends avudama",
        "you have nice profile", "looking so attractive", "let's talk in private"
    )

    private val VIDEO_CALL_SCAM_PHRASES = listOf(
        "let's do a video call", "video call cheddam", "video call lo matladu",
        "video chat", "video call lo chupinchu", "come on video call", "facetime"
    )

    private val PHOTO_REQUEST_PHRASES = listOf(
        "send me a photo", "send your photo", "send private photo", "send me pic",
        "send nude", "share your photo", "photo pampandi", "pic pampandi",
        "private pic", "send full photo", "nee photo pampu", "chupinchava"
    )

    private val THREAT_AND_EXTORTION_PHRASES = listOf(
        "i will share your photo", "i will expose you", "i will tell everyone",
        "pay or i will", "blackmail", "threatening", "nee photo share chestanu",
        "nee friends ki chupistanu", "police ki cheppistanu", "video viral chestanu",
        "send money or i will post on youtube", "facebook lo upload chestanu",
        "pay 10000", "send 50000", "send money urgently", "pay or face consequences"
    )

    private val MONEY_REQUEST_PHRASES = listOf(
        "send money", "send some money", "send me money", "transfer money",
        "send rs", "send rupees", "need money urgently", "emergency send",
        "dabbu pampandi", "money pampandi", "paytm lo pampandi", "upi lo pampandi",
        "phonepe cheyyi", "googlepay cheyyi", "send 5000", "send 10000"
    )

    fun analyzeConversation(messages: List<String>): HoneyTrapAnalysis {
        val triggers = mutableListOf<HoneyTrapTrigger>()
        var riskScore = 0

        for (message in messages) {
            val msgLower = message.lowercase()

            // 1. Romantic escalation
            val romantic = ROMANTIC_ESCALATION_PHRASES.firstOrNull { msgLower.contains(it) }
            if (romantic != null && triggers.none { it.type == TriggerType.ROMANTIC_ESCALATION }) {
                triggers.add(HoneyTrapTrigger(TriggerType.ROMANTIC_ESCALATION, "Romantic opening from unknown contact", romantic))
                riskScore += 15
            }

            // 2. Video call solicitation
            val videoCall = VIDEO_CALL_SCAM_PHRASES.firstOrNull { msgLower.contains(it) }
            if (videoCall != null && triggers.none { it.type == TriggerType.VIDEO_CALL_INVITE }) {
                triggers.add(HoneyTrapTrigger(TriggerType.VIDEO_CALL_INVITE, "Private video call invitation", videoCall))
                riskScore += 20
            }

            // 3. Photo requests
            val photo = PHOTO_REQUEST_PHRASES.firstOrNull { msgLower.contains(it) }
            if (photo != null && triggers.none { it.type == TriggerType.PHOTO_REQUEST }) {
                triggers.add(HoneyTrapTrigger(TriggerType.PHOTO_REQUEST, "Request for private photographs", photo))
                riskScore += 30
            }

            // 4. Threats & Extortion
            val threat = THREAT_AND_EXTORTION_PHRASES.firstOrNull { msgLower.contains(it) }
            if (threat != null && triggers.none { it.type == TriggerType.THREAT_BLACKMAIL }) {
                triggers.add(HoneyTrapTrigger(TriggerType.THREAT_BLACKMAIL, "Extortion & viral exposure threat", threat))
                riskScore += 45
            }

            // 5. Money demands
            val money = MONEY_REQUEST_PHRASES.firstOrNull { msgLower.contains(it) }
            if (money != null && triggers.none { it.type == TriggerType.MONEY_REQUEST }) {
                triggers.add(HoneyTrapTrigger(TriggerType.MONEY_REQUEST, "Financial extortion demand", money))
                riskScore += 30
            }
        }

        // Multi-trigger escalation multiplier
        val triggerTypes = triggers.map { it.type }.toSet()
        if (triggerTypes.contains(TriggerType.PHOTO_REQUEST) && triggerTypes.contains(TriggerType.MONEY_REQUEST)) {
            riskScore += 20
        }
        if (triggerTypes.contains(TriggerType.THREAT_BLACKMAIL)) {
            riskScore = maxOf(riskScore, 75)
        }

        val clampedScore = riskScore.coerceIn(0, 100)
        val riskLevel = when {
            clampedScore >= 60 -> RiskLevel.RED
            clampedScore >= 25 -> RiskLevel.ORANGE
            else -> RiskLevel.GREEN
        }

        val explanation = buildExplanation(riskLevel, triggers, clampedScore)

        return HoneyTrapAnalysis(
            riskLevel = riskLevel,
            riskScore = clampedScore,
            triggers = triggers,
            explanation = explanation
        )
    }

    fun analyzeMessage(message: String): HoneyTrapAnalysis =
        analyzeConversation(listOf(message))

    private fun buildExplanation(
        level: RiskLevel,
        triggers: List<HoneyTrapTrigger>,
        score: Int
    ): String {
        if (triggers.isEmpty()) return "No honey trap indicators detected."
        val types = triggers.map { it.type }.distinct()
        val sb = StringBuilder()
        sb.append("Risk score: $score/100. ")
        if (TriggerType.THREAT_BLACKMAIL in types) {
            sb.append("🚨 అత్యవసరం: వీడియో లేదా ఫోటోలను లీక్ చేస్తామని బెదిరింపు గుర్తించబడింది. డబ్బు ఎప్పుడూ పంపకండి. ")
        }
        if (TriggerType.PHOTO_REQUEST in types) sb.append("ప్రైవేట్ ఫోటోలు పంపమని అడిగారు. ")
        if (TriggerType.MONEY_REQUEST in types) sb.append("డబ్బు డిమాండ్ చేశారు. ")
        if (TriggerType.ROMANTIC_ESCALATION in types) sb.append("పరిచయం లేని వ్యక్తి నుండి శీఘ్ర ప్రేమపూర్వక సంభాషణ. ")
        sb.append("\nహెచ్చరిక: 'Trap Lo Padakandi. Photo / Money Pampakandi.' వెంటనే నంబర్ బ్లాక్ చేయండి, 1930 కి కాల్ చేయండి.")
        return sb.toString()
    }
}
