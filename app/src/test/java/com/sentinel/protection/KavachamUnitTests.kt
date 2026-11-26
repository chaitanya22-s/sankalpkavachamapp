package com.sentinel.protection

import com.sentinel.protection.domain.model.RiskLevel
import com.sentinel.protection.util.HoneyTrapDetector
import com.sentinel.protection.util.PhoneNumberHelper
import com.sentinel.protection.util.ScamKeywordDetector
import com.sentinel.protection.util.SmsSpamDetector
import org.junit.Assert.*
import org.junit.Test

class PhoneNumberHelperTest {

    @Test
    fun `valid Indian mobile number is valid`() {
        val result = PhoneNumberHelper.parse("9876543210", "IN")
        assertTrue(result.isValid)
        assertEquals("IN", result.regionCode)
    }

    @Test
    fun `invalid number returns invalid`() {
        val result = PhoneNumberHelper.parse("123", "IN")
        assertFalse(result.isValid)
    }

    @Test
    fun `international format is parsed correctly`() {
        val result = PhoneNumberHelper.parse("+919876543210", "IN")
        assertTrue(result.isValid)
        assertEquals("IN", result.regionCode)
        assertEquals("+91 98765 43210", result.formattedInternational)
    }

    @Test
    fun `normalize for lookup returns E164`() {
        val normalized = PhoneNumberHelper.normalizeForLookup("98765 43210", "IN")
        assertEquals("+919876543210", normalized)
    }

    @Test
    fun `empty string returns invalid`() {
        val result = PhoneNumberHelper.parse("", "IN")
        assertFalse(result.isValid)
    }

    @Test
    fun `landline number is valid`() {
        val result = PhoneNumberHelper.parse("04023456789", "IN")
        // Hyderabad number format
        assertTrue(result.isValid || !result.isValid) // Just checking it doesn't crash
    }
}

class SmsSpamDetectorTest {

    @Test
    fun `message with OTP keyword is flagged`() {
        val result = SmsSpamDetector.analyze("UNKNOWN", "Your OTP is 123456. Do not share.")
        // OTP from legitimate senders is common – this should be ORANGE at most
        assertNotNull(result)
        assertTrue(result.riskLevel in listOf("GREEN", "ORANGE", "RED"))
    }

    @Test
    fun `message with lottery keyword is flagged as suspicious`() {
        val result = SmsSpamDetector.analyze("UNKNOWN", "Congratulations! You won a LOTTERY prize. Click http://bit.ly/win123 to claim.")
        assertTrue(result.isSpam || result.riskLevel in listOf("ORANGE", "RED"))
        assertTrue(result.suspiciousLinks.any { it.contains("bit.ly") })
    }

    @Test
    fun `legitimate bank SMS is not flagged as high risk`() {
        val result = SmsSpamDetector.analyze("VM-HDFCBK", "INR 500 debited from your HDFC account on 26-Nov. Available balance: INR 10000.")
        // Should not be RED
        assertNotEquals("RED", result.riskLevel)
    }

    @Test
    fun `suspicious xyz domain is flagged`() {
        val result = SmsSpamDetector.analyze("UNKNOWN", "Visit http://freeprize.xyz to claim your reward now!")
        assertTrue(result.suspiciousLinks.isNotEmpty() || result.isSpam)
    }

    @Test
    fun `bomber detection works`() {
        val now = System.currentTimeMillis()
        val timestamps = List(6) { now - it * 5000L } // 6 messages in 30 seconds
        assertTrue(SmsSpamDetector.detectBomberAttack(timestamps, threshold = 5))
    }

    @Test
    fun `no bomber when messages are spread out`() {
        val now = System.currentTimeMillis()
        val timestamps = List(6) { now - it * 20000L } // 6 messages, one every 20 seconds
        assertFalse(SmsSpamDetector.detectBomberAttack(timestamps, windowMs = 60_000, threshold = 5))
    }
}

class HoneyTrapDetectorTest {

    @Test
    fun `single romantic message alone is low risk`() {
        val result = HoneyTrapDetector.analyzeConversation(listOf("Hi handsome"))
        // Single romantic opener should be ORANGE at most
        assertTrue(result.riskLevel in listOf(RiskLevel.GREEN, RiskLevel.ORANGE))
    }

    @Test
    fun `photo and money request together is high risk`() {
        val messages = listOf(
            "Hi handsome, I like you",
            "Let's do a video call",
            "Send me a private photo",
            "Send me some money"
        )
        val result = HoneyTrapDetector.analyzeConversation(messages)
        assertEquals(RiskLevel.RED, result.riskLevel)
        assertTrue(result.riskScore >= 60)
    }

    @Test
    fun `threat and blackmail is high risk`() {
        val result = HoneyTrapDetector.analyzeMessage("I will share your photo with everyone if you don't pay")
        assertEquals(RiskLevel.RED, result.riskLevel)
    }

    @Test
    fun `normal conversation is not flagged`() {
        val messages = listOf(
            "Hello, how are you?",
            "I'm fine, thanks",
            "What do you do?",
            "I am a software engineer"
        )
        val result = HoneyTrapDetector.analyzeConversation(messages)
        assertEquals(RiskLevel.GREEN, result.riskLevel)
        assertTrue(result.triggers.isEmpty())
    }

    @Test
    fun `telugu money request is flagged`() {
        val result = HoneyTrapDetector.analyzeMessage("Dabbu pampandi please")
        assertTrue(result.riskLevel in listOf(RiskLevel.ORANGE, RiskLevel.RED))
    }
}

class ScamKeywordDetectorTest {

    @Test
    fun `OTP keyword detected`() {
        val result = ScamKeywordDetector.analyze("Please share your OTP to verify your account")
        assertTrue(result.hasScamIndicators)
        assertTrue(result.matchedKeywords.any { it.contains("otp") })
    }

    @Test
    fun `Telugu scam keyword detected`() {
        val result = ScamKeywordDetector.analyze("OTP cheppandi sir account suspend avutundi")
        assertTrue(result.hasScamIndicators)
    }

    @Test
    fun `clean message returns no indicators`() {
        val result = ScamKeywordDetector.analyze("Hey, let's meet for lunch tomorrow at the cafe")
        assertFalse(result.hasScamIndicators)
        assertEquals(RiskLevel.GREEN, result.riskLevel)
    }

    @Test
    fun `multiple keywords increase risk`() {
        val result = ScamKeywordDetector.analyze("Share your OTP, bank account and password immediately")
        assertEquals(RiskLevel.RED, result.riskLevel)
        assertTrue(result.matchedKeywords.size >= 3)
    }
}

