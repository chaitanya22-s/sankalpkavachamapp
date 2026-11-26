package com.sentinel.protection.service

import android.app.Activity
import android.os.Bundle
import com.sentinel.protection.MainActivity

/**
 * Stub activity required for the SMS popup intent filter.
 * When Sankalp Kavacham is the default SMS app, this activity handles the SENDTO intent.
 * It redirects to MainActivity which contains the full SMS experience.
 */
class SmsPopupActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = android.content.Intent(this, MainActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                    android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("destination", "sms")
        }
        startActivity(intent)
        finish()
    }
}

