# 🛡️ Sankalp Kavacham (సంకల్ప్ కవచం)
### India's Smart Call, Honey Trap & Cyber Safety Application
> *"Truecaller Kante Better – 100% Free, No Ads, Made for India"*

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/)
[![Language](https://img.shields.io/badge/Kotlin-2.0+-purple.svg)](https://kotlinlang.org/)
[![UI](https://img.shields.io/badge/Jetpack_Compose-Material_3-blue.svg)](https://developer.android.com/jetpack/compose)
[![Backend](https://img.shields.io/badge/Backend-Supabase_Realtime-3ECF8E.svg)](https://supabase.com/)
[![License](https://img.shields.io/badge/License-MIT-brightgreen.svg)](LICENSE)

---

## 🚀 Overview

**Sankalp Kavacham** is a production-grade, privacy-first cybersecurity and smart caller assistance application built for Indian users. Designed with a **Telugu-first** approach and accessible large-typography interface for senior citizens, it actively protects citizens from:

- ⚡ **Electricity Bill Disconnection Scams**
- 🚗 **Traffic E-Challan Fake APK Malware**
- 💼 **Part-Time Telegram Task Scams**
- 👮 **Digital Arrest / CBI Impersonation Scams**
- 🏦 **SBI YONO / PAN Deactivation Phishing**
- 📦 **India Post Customs Parcel Traps**
- 🚨 **Honey Trap & Private Video Extortion**
- 💣 **SMS Bomber Burst Attacks**

---

## ✨ Key Features

### 1. 📞 Smart Dialer & Call Protection
- Fully functional Android dialer with numerical keypad (`*`, `#`, `0`-`9`), Recents, and Contacts.
- On-device number normalization using Google's `libphonenumber`.
- Android `CallScreeningService` that screens, silences, or blocks community-flagged numbers automatically.
- Real-time in-app scam phrase detection (*"OTP cheppandi"*, *"Dabbu pampandi"*, *"KYC block"*).

### 2. ⚡ Supabase Community Backend with Realtime Sync
- **10-User Auto-Verification Rule**: When 10 users report the same fraudulent number, it is automatically verified as **`SPAM`** across India with zero delay.
- Real-time WebSocket synchronization (`supabase_realtime`): Any reported number or threat is instantly reflected on all devices.
- Offline fallback: High-risk community numbers automatically sync into local **Room Database** so call screening works even without internet.

### 3. 🚨 Honey Trap Detection Engine
- Analyzes multi-turn sequential conversations for the classic honey trap progression:
  1. Unsolicited romantic opening (*"Hi handsome / cute"*)
  2. Urgent private video call invitation
  3. Demands for private photos
  4. Extortion threats to leak videos to family/social media unless money is paid
- Clear Telugu warning: **"Trap Lo Padakandi. Photo / Money Pampakandi."**
- Immediate 1-tap Block, Report, and 1930 Helpline actions.

### 4. 🛡️ Advanced SMS Fraud Scanner
- Signature matching for 9 Indian fraud vectors.
- Direct malicious `.apk` download link detection inside SMS.
- URL shortener and brand homograph/typosquatting checks.
- SMS bomber burst detector (flags > 5 messages in 60 seconds).

### 5. 🎙️ Kavacham Amma (Voice Assistant)
- Local speech recognition and text-to-speech engine supporting Telugu and English.
- Instant spoken guidance on OTP safety, cybercrime reporting, and fraud prevention without paid third-party AI APIs.

### 6. 📸 Media Authenticity Checker
- On-device ML Kit Face Detection assessing eye-openness asymmetry and rotation indicators.
- Private photos and videos are processed purely on-device and never uploaded.

### 7. 🔒 Privacy Centre & 1930 Cyber Help
- Zero advertisements, zero monetization of user contacts, on-device processing.
- One-touch dialer for the Indian National Cyber Crime Helpline: **1930**.
- Direct link to the official Indian cybercrime reporting portal: [cybercrime.gov.in](https://cybercrime.gov.in).

---

## 🛠️ Architecture & Tech Stack

```
com.sentinel.protection
├── data
│   ├── local (Room DB: KavachamDatabase, Daos, Entities)
│   ├── remote (SupabaseConfig, SupabaseDtos, SupabaseSpamRepository)
│   └── repository (Community Spam Sync, Offline Cache)
├── domain
│   └── model (Models, RiskLevel, HoneyTrapAnalysis)
├── service
│   ├── KavachamCallScreeningService (Android Telecom)
│   ├── SmsReceiver (Telephony SMS Broadcast)
│   └── BootReceiver
├── ui
│   ├── navigation (KavachamNavGraph, Adaptive Phone/Tablet layout)
│   ├── theme (Aurora Emerald Green, Accessible Typography)
│   └── screens (Dashboard, Dialer, SpamBox, HoneyTrap, Assistant, Media, Privacy)
└── util (PhoneNumberHelper, SmsSpamDetector, HoneyTrapDetector, ScamKeywordDetector)
```

- **Language:** Kotlin 2.0+
- **Architecture:** Clean Architecture + MVVM + Repository Pattern
- **UI Framework:** Jetpack Compose + Material 3
- **Dependency Injection:** Dagger Hilt
- **Local Persistence:** Room Database + DataStore Preferences
- **Cloud Backend:** Supabase (PostgreSQL + PostgREST + Realtime WebSockets)
- **Validation:** Google `libphonenumber`
- **Vision:** Google ML Kit Face Detection

---

## 🗄️ Supabase Database Setup

Run the SQL migration script from [supabase_schema.sql](supabase_schema.sql) in your **Supabase Dashboard ➔ SQL Editor**:

```sql
-- 1. spam_numbers: number, reason, reported_count, is_verified_spam, created_at
-- 2. spam_messages: sender, message, type, is_spam, created_at
-- 3. reports: user_number, fraud_number, message, type, created_at
-- Includes automated trigger for 10-threshold community verification and Realtime publication.
```

---

## 💻 Running the Live Preview

Run the standalone interactive preview on `localhost`:

```bash
python -m http.server 8080 --directory "preview"
```
Open **[http://localhost:8080](http://localhost:8080)** in your browser to test all features, Supabase Realtime sync, phone/tablet layouts, and Telugu voice assistant.

---

## 📱 Building the Android APK

Open this directory in **Android Studio** or compile via terminal:

```bash
# Debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test
```

---

## 🇮🇳 Made for India
Built with ❤️ to keep Indian citizens safe from online fraud, scams, and digital exploitation.
