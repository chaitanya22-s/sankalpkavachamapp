package com.sentinel.protection.util

import android.util.Patterns
import java.net.URI

object SmsSpamDetector {

    enum class ScamCategory(val displayName: String, val teluguAdvice: String) {
        ELECTRICITY_BILL(
            "Electricity Disconnection Fraud",
            "కరెంట్ ఆఫీస్ అధికారులు ఎప్పుడూ ఫోన్ చేసి బిల్లు కట్టకపోతే పవర్ కట్ చేస్తామని బెదిరించరు. ఎవరికీ కాల్ చేయకండి."
        ),
        TRAFFIC_CHALLAN_APK(
            "Fake E-Challan / Malicious APK",
            "మెసేజ్‌లలో వచ్చే లింక్స్ నుండి ఎప్పుడూ APK ఫైల్స్ డౌన్‌లోడ్ చేయకండి. ఇది మీ ఫోన్ లోని డేటాను దొంగిలిస్తుంది."
        ),
        PART_TIME_JOB(
            "Part-Time Job / Telegram Task Scam",
            "యూట్యూబ్ లైక్‌లు, రేటింగ్స్‌తో రోజుకు వేల రూపాయలు వస్తాయని చెప్పే స్కామ్‌లను నమ్మకండి. డబ్బు ఎప్పుడూ పంపకండి."
        ),
        DIGITAL_ARREST(
            "Digital Arrest / Police Impersonation",
            "పోలీసులు లేదా సీబీఐ ఎప్పుడూ వాట్సాప్ లేదా స్కైప్ వీడియో కాల్ ద్వారా అరెస్ట్ చేయరు. భయపడి డబ్బు పంపకండి."
        ),
        COURIER_CUSTOMS(
            "Fake India Post / Customs Parcel Scam",
            "అడ్రస్ అప్డేట్ లేదా కస్టమ్స్ డ్యూటీ కోసం వచ్చే అనుమానాస్పద లింకులను క్లిక్ చేయకండి. ఇండియా పోస్ట్ అధికారిక సైట్ లో చెక్ చేయండి."
        ),
        BANK_KYC_YONO(
            "Bank Account / KYC Deactivation Fraud",
            "బ్యాంక్ ఉద్యోగులు ఎప్పుడూ పాన్ లేదా ఆధార్ అప్డేట్ కోసం లింకులు పంపరు. బ్యాంకును నేరుగా సంప్రదించండి."
        ),
        LOTTERY_PRIZE(
            "Fake Lottery / KBC Cash Prize",
            "మీరు పాల్గొనని లాటరీలలో డబ్బు రాదు. లాటరీ క్లెయిమ్ కోసం ఎటువంటి ప్రాసెసింగ్ ఫీజు చెల్లించకండి."
        ),
        INSTANT_LOAN(
            "Predatory Loan / Zero Interest Trap",
            "డాక్యుమెంట్లు లేకుండా తక్కువ వడ్డీకి లోన్ ఇస్తామని చెప్పే నకిలీ లోన్ యాప్‌ల లింకులను క్లిక్ చేయకండి."
        ),
        HONEY_TRAP(
            "Honey Trap / Video Extortion",
            "పరిచయం లేని వ్యక్తులతో ప్రైవేట్ వీడియో కాల్స్ మాట్లాడకండి. బ్లాక్‌మెయిల్ చేస్తే వెంటనే 1930 కి ఫిర్యాదు చేయండి."
        ),
        GENERAL_PHISHING(
            "Suspicious Phishing / Fraud",
            "అనుమానాస్పద లింకులను తెరవకండి లేదా వ్యక్తిగత సమాచారాన్ని నమోదు చేయకండి."
        ),
        SAFE(
            "Clean / Legitimate Message",
            "ఈ మెసేజ్‌లో ఎటువంటి ప్రమాదకర అంశాలు గుర్తించబడలేదు."
        )
    }

    data class SpamAnalysis(
        val isSpam: Boolean,
        val riskLevel: String, // GREEN, ORANGE, RED
        val category: ScamCategory,
        val reasons: List<String>,
        val suspiciousLinks: List<String>,
        val detectedKeywords: List<String>,
        val teluguWarning: String,
        val confidence: Float // 0.0 - 1.0
    )

    // Category-specific signatures
    private val ELECTRICITY_KEYWORDS = listOf(
        "electricity", "power cut", "power will be disconnected", "bill unpaid",
        "power supply", "electricity officer", "tgspdcl", "apspdcl", "tsepdcl",
        "apepdcl", "bescom", "tneb", "discom", "current bill", "current cut",
        "కరెంట్ బిల్లు", "పవర్ కట్", "కరెంట్ ఆఫీసర్"
    )

    private val CHALLAN_APK_KEYWORDS = listOf(
        "echallan", "e-challan", "challan pending", "traffic police", "mparivahan",
        "court notice", "vehicle challan", ".apk", "download apk", "install app to pay",
        "చలాన్ పెండింగ్", "ట్రాఫిక్ చలాన్"
    )

    private val JOB_SCAM_KEYWORDS = listOf(
        "daily income", "part time job", "work from home", "like youtube",
        "subscribe video", "earn 3000", "earn 5000", "daily earning", "telegram task",
        "review task", "hotel rating task", "daily 2000-5000", "task completed",
        "పార్ట్ టైమ్ జాబ్", "రోజూ సంపాదించండి"
    )

    private val DIGITAL_ARREST_KEYWORDS = listOf(
        "digital arrest", "cbi notice", "delhi police", "mumbai police", "narcotics",
        "customs seized", "illegal parcel", "passport seized", "warrant issued",
        "cyber crime department", "trai block", "sim card disconnect within 2 hours",
        "డిజిటల్ అరెస్ట్", "పోలీస్ నోటీస్", "పార్శిల్ పట్టుబడింది"
    )

    private val COURIER_KEYWORDS = listOf(
        "india post", "package held", "incomplete address", "customs duty",
        "delivery attempt failed", "package returned", "fedex parcel", "dhl package",
        "update address within 24 hours", "delivery fee pending", "ఇండియా పోస్ట్", "పార్శిల్"
    )

    private val BANK_KYC_KEYWORDS = listOf(
        "yono block", "yono account", "sbi account suspended", "kyc expired",
        "pan card not linked", "account will be deactivated", "debit card blocked",
        "update pan", "netbanking suspended", "icici kyc", "hdfc netbanking",
        "కేవైసీ అప్డేట్", "అకౌంట్ బ్లాక్", "పాన్ కార్డ్"
    )

    private val LOTTERY_KEYWORDS = listOf(
        "kbc lottery", "kaun banega crorepati", "won lottery", "cash prize",
        "25 lakh", "winner of lucky draw", "claim reward", "congratulations you have won",
        "లాటరీ గెలిచారు", "లక్కీ డ్రా", "బహుమతి"
    )

    private val LOAN_KEYWORDS = listOf(
        "pre-approved loan", "instant loan", "loan approved 5 lakh", "no cibil",
        "0% interest", "instant disbursal to bank", "apply for personal loan now",
        "తక్కువ వడ్డీకి లోన్", "ఇన్‌స్టంట్ లోన్"
    )

    private val HONEY_TRAP_KEYWORDS = listOf(
        "hi handsome", "send private photo", "send nude", "video call nude",
        "share your video with friends", "pay or video viral", "paytm send",
        "ఫోటో పంపండి", "వీడియో వైరల్", "డబ్బు పంపండి"
    )

    // Suspicious TLD patterns
    private val SUSPICIOUS_TLDS = listOf(
        ".xyz", ".top", ".tk", ".ml", ".ga", ".cf", ".icu", ".buzz", ".club",
        ".site", ".vip", ".work", ".cn", ".click", ".link", ".live", ".fit", ".rest"
    )

    // Suspicious shorteners
    private val SUSPICIOUS_SHORTENERS = listOf(
        "bit.ly", "tinyurl.com", "t.co", "ow.ly", "is.gd", "buff.ly",
        "cutt.ly", "rb.gy", "shorturl.at", "qr.ae", "s.id", "v.gd"
    )

    // Free cloud/hosting domains often abused for phishing
    private val ABUSED_PHISHING_HOSTS = listOf(
        "ngrok.io", "vercel.app", "glitch.me", "firebaseapp.com",
        "000webhostapp.com", "pages.dev", "netlify.app", "render.com",
        "weebly.com", "wixsite.com", "blogspot.com"
    )

    // Legitimate DLT Indian bank/gov sender prefixes
    private val LEGITIMATE_SENDER_PREFIXES = listOf(
        "VM-", "VK-", "AD-", "BZ-", "TA-", "TM-", "JM-", "GP-", "MX-",
        "AX-", "BK-", "JD-", "SB-", "BL-"
    )

    // Known trusted financial & government sender names
    private val TRUSTED_SENDERS = listOf(
        "HDFCBK", "SBINB", "ICICIB", "AXISBK", "KOTAKB", "UNIONB",
        "AIRTEL", "JIOFBR", "EPFOHO", "CBSSBI", "UIDAI", "ITRPRC", "IRCTC"
    )

    fun analyze(sender: String, body: String): SpamAnalysis {
        val reasons = mutableListOf<String>()
        val suspiciousLinks = mutableListOf<String>()
        val detectedKeywords = mutableListOf<String>()
        var riskScore = 0

        val bodyLower = body.lowercase()
        val senderUpper = sender.uppercase()

        // 1. Detect Category & Matches
        var category = ScamCategory.SAFE

        fun checkCategory(keywords: List<String>, cat: ScamCategory, baseScore: Int, reasonPrefix: String): Boolean {
            val matches = keywords.filter { bodyLower.contains(it.lowercase()) }
            if (matches.isNotEmpty()) {
                detectedKeywords.addAll(matches)
                riskScore += baseScore + (matches.size * 10)
                reasons.add("$reasonPrefix: ${matches.take(3).joinToString(", ")}")
                if (category == ScamCategory.SAFE || baseScore > 40) {
                    category = cat
                }
                return true
            }
            return false
        }

        checkCategory(ELECTRICITY_KEYWORDS, ScamCategory.ELECTRICITY_BILL, 55, "Electricity bill fraud indicators")
        checkCategory(CHALLAN_APK_KEYWORDS, ScamCategory.TRAFFIC_CHALLAN_APK, 60, "Fake challan / APK trap indicators")
        checkCategory(JOB_SCAM_KEYWORDS, ScamCategory.PART_TIME_JOB, 50, "Part-time job / Telegram task scam indicators")
        checkCategory(DIGITAL_ARREST_KEYWORDS, ScamCategory.DIGITAL_ARREST, 65, "Digital arrest / Police impersonation indicators")
        checkCategory(COURIER_KEYWORDS, ScamCategory.COURIER_CUSTOMS, 45, "Fake courier / India Post parcel scam indicators")
        checkCategory(BANK_KYC_KEYWORDS, ScamCategory.BANK_KYC_YONO, 55, "Bank account / KYC suspension trap indicators")
        checkCategory(LOTTERY_KEYWORDS, ScamCategory.LOTTERY_PRIZE, 50, "Fake lottery / prize winner indicators")
        checkCategory(LOAN_KEYWORDS, ScamCategory.INSTANT_LOAN, 40, "Predatory loan / zero interest scam indicators")
        checkCategory(HONEY_TRAP_KEYWORDS, ScamCategory.HONEY_TRAP, 60, "Honey trap / extortion indicators")

        // 2. Link & URL Analysis
        val urls = extractUrls(body)
        for (url in urls) {
            val urlLower = url.lowercase()

            // Dangerous direct APK download link
            if (urlLower.contains(".apk") || urlLower.endsWith(".apk")) {
                suspiciousLinks.add(url)
                riskScore += 45
                reasons.add("CRITICAL DANGER: Direct APK app download link detected ($url)")
                if (category == ScamCategory.SAFE) category = ScamCategory.TRAFFIC_CHALLAN_APK
            }

            // Suspicious TLD / Shortener
            if (isSuspiciousUrl(url)) {
                suspiciousLinks.add(url)
                riskScore += 30
                reasons.add("Suspicious / Unofficial URL detected ($url)")
            }

            // IP address as host
            if (isRawIpUrl(url)) {
                suspiciousLinks.add(url)
                riskScore += 40
                reasons.add("Dangerous: Raw IP address used in link ($url)")
            }

            // Bank brand in unverified domain
            if (hasBankBrandSquatting(urlLower)) {
                suspiciousLinks.add(url)
                riskScore += 45
                reasons.add("Brand spoofing: Bank brand name used inside an unofficial domain ($url)")
            }
        }

        // 3. Sender Verification (Indian DLT Framework)
        val isSenderTrusted = TRUSTED_SENDERS.any { senderUpper.contains(it) }
        val isSenderDltFormat = LEGITIMATE_SENDER_PREFIXES.any { senderUpper.startsWith(it) }

        if (isSenderTrusted && suspiciousLinks.isEmpty() && category == ScamCategory.SAFE) {
            // Highly trusted transactional message (e.g. routine debit/credit SMS)
            riskScore = 0
            category = ScamCategory.SAFE
        } else if (isSenderDltFormat && suspiciousLinks.isEmpty() && riskScore < 40) {
            riskScore = maxOf(0, riskScore - 20)
        } else if (!isSenderDltFormat && (bodyLower.contains("bank") || bodyLower.contains("sbi") || bodyLower.contains("hdfc"))) {
            // Unverified sender pretending to be a bank
            riskScore += 25
            reasons.add("Sender ($sender) is NOT a registered official DLT bank sender ID")
        }

        // 4. Urgency & Coercion triggers
        if (bodyLower.contains("immediately") || bodyLower.contains("tonight by") ||
            bodyLower.contains("within 24 hours") || bodyLower.contains("urgent action")) {
            riskScore += 15
            reasons.add("Artificial urgency detected (common pressure tactic)")
        }

        // Final score calculation
        val clampedScore = riskScore.coerceIn(0, 100)
        val riskLevel = when {
            clampedScore >= 55 -> "RED"
            clampedScore >= 25 -> "ORANGE"
            else -> "GREEN"
        }
        val isSpam = clampedScore >= 45

        if (category == ScamCategory.SAFE && isSpam) {
            category = ScamCategory.GENERAL_PHISHING
        }

        return SpamAnalysis(
            isSpam = isSpam,
            riskLevel = riskLevel,
            category = category,
            reasons = reasons,
            suspiciousLinks = suspiciousLinks,
            detectedKeywords = detectedKeywords.distinct(),
            teluguWarning = category.teluguAdvice,
            confidence = clampedScore / 100f
        )
    }

    private fun extractUrls(text: String): List<String> {
        val matcher = Patterns.WEB_URL.matcher(text)
        val urls = mutableListOf<String>()
        while (matcher.find()) {
            urls.add(matcher.group())
        }
        return urls
    }

    private fun isSuspiciousUrl(url: String): Boolean {
        return try {
            val uri = URI(if (url.startsWith("http")) url else "https://$url")
            val host = uri.host?.lowercase() ?: return false
            SUSPICIOUS_TLDS.any { host.endsWith(it) } ||
                SUSPICIOUS_SHORTENERS.any { host == it || host.endsWith(".$it") } ||
                ABUSED_PHISHING_HOSTS.any { host.endsWith(it) }
        } catch (e: Exception) {
            false
        }
    }

    private fun isRawIpUrl(url: String): Boolean {
        return try {
            val uri = URI(if (url.startsWith("http")) url else "https://$url")
            val host = uri.host ?: return false
            // Regex for IPv4
            host.matches(Regex("""\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}"""))
        } catch (e: Exception) {
            false
        }
    }

    private fun hasBankBrandSquatting(urlLower: String): Boolean {
        val brands = listOf("sbi", "yono", "hdfc", "icici", "axis", "kotak", "incometax", "parivahan")
        val legitDomains = listOf("sbi.co.in", "hdfcbank.com", "icicibank.com", "axisbank.com", "kotak.com", "incometax.gov.in", "parivahan.gov.in")

        val hasBrand = brands.any { urlLower.contains(it) }
        val isLegit = legitDomains.any { urlLower.contains(it) }

        return hasBrand && !isLegit
    }

    fun detectBomberAttack(recentMessages: List<Long>, windowMs: Long = 60_000, threshold: Int = 5): Boolean {
        if (recentMessages.size < threshold) return false
        val now = System.currentTimeMillis()
        val inWindow = recentMessages.count { now - it <= windowMs }
        return inWindow >= threshold
    }
}
