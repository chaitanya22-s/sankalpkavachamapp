package com.sentinel.protection.data.repository

import android.content.Context
import com.sentinel.protection.data.local.dao.BlockedNumberDao
import com.sentinel.protection.data.local.dao.SpamReportDao
import com.sentinel.protection.data.local.entity.BlockedNumberEntity
import com.sentinel.protection.data.local.entity.SpamReportEntity
import com.sentinel.protection.data.remote.SupabaseConfig
import com.sentinel.protection.data.remote.model.ReportDto
import com.sentinel.protection.data.remote.model.SpamMessageDto
import com.sentinel.protection.data.remote.model.SpamNumberDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseSpamRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val spamReportDao: SpamReportDao,
    private val blockedNumberDao: BlockedNumberDao
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    /**
     * Submit a report for a fraud/spam number.
     * Inserts into Supabase `reports` table.
     * The Supabase database trigger automatically increments `reported_count` in `spam_numbers`.
     * If 10 users report it, it automatically becomes verified SPAM!
     */
    suspend fun reportNumber(
        fraudNumber: String,
        reason: String,
        userNumber: String = "Anonymous",
        message: String = "",
        type: String = "Spam Call"
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val url = SupabaseConfig.getSupabaseUrl(context).first()
            val anonKey = SupabaseConfig.getSupabaseAnonKey(context).first()

            val report = ReportDto(
                userNumber = userNumber,
                fraudNumber = fraudNumber,
                message = if (message.isNotBlank()) message else reason,
                type = type
            )

            val payload = json.encodeToString(report)
            val endpoint = "$url/rest/v1/reports"

            val responseCode = makeHttpRequest(
                endpoint = endpoint,
                method = "POST",
                payload = payload,
                anonKey = anonKey
            )

            if (responseCode in 200..299) {
                // Also store report locally in Room
                spamReportDao.insert(
                    SpamReportEntity(
                        phoneNumber = fraudNumber,
                        category = type,
                        notes = reason,
                        reportedByUser = true
                    )
                )
                Timber.d("Successfully reported $fraudNumber to Supabase")
                Result.success(Unit)
            } else {
                Result.failure(Exception("Supabase HTTP Error: $responseCode"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to submit report to Supabase")
            Result.failure(e)
        }
    }

    /**
     * Fetch community reported spam numbers from Supabase `spam_numbers`.
     * Syncs numbers with >= 10 reports directly into the local block list / spam cache!
     */
    suspend fun fetchCommunitySpamNumbers(): Result<List<SpamNumberDto>> = withContext(Dispatchers.IO) {
        try {
            val url = SupabaseConfig.getSupabaseUrl(context).first()
            val anonKey = SupabaseConfig.getSupabaseAnonKey(context).first()

            val endpoint = "$url/rest/v1/spam_numbers?select=*&order=reported_count.desc&limit=200"

            val (code, responseBody) = makeHttpGet(endpoint, anonKey)
            if (code in 200..299) {
                val numbers = json.decodeFromString<List<SpamNumberDto>>(responseBody)

                // Sync high-risk numbers into local DB so offline CallScreening blocks them!
                numbers.forEach { item ->
                    if (item.reportedCount >= 10 || item.isVerifiedSpam) {
                        blockedNumberDao.insert(
                            BlockedNumberEntity(
                                phoneNumber = item.number,
                                reason = "Community Verified Spam (${item.reportedCount} reports): ${item.reason}"
                            )
                        )
                    }
                }
                Result.success(numbers)
            } else {
                Result.failure(Exception("Supabase GET error: $code"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching spam numbers from Supabase")
            Result.failure(e)
        }
    }

    /**
     * Check if a specific phone number has community spam reports in Supabase.
     */
    suspend fun checkNumberReputation(phoneNumber: String): Result<SpamNumberDto?> = withContext(Dispatchers.IO) {
        try {
            val url = SupabaseConfig.getSupabaseUrl(context).first()
            val anonKey = SupabaseConfig.getSupabaseAnonKey(context).first()

            val endpoint = "$url/rest/v1/spam_numbers?number=eq.$phoneNumber&select=*"
            val (code, responseBody) = makeHttpGet(endpoint, anonKey)

            if (code in 200..299) {
                val list = json.decodeFromString<List<SpamNumberDto>>(responseBody)
                Result.success(list.firstOrNull())
            } else {
                Result.failure(Exception("Supabase lookup failed with code $code"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Supabase lookup error for $phoneNumber")
            Result.failure(e)
        }
    }

    /**
     * Save an identified spam message to Supabase `spam_messages`.
     */
    suspend fun reportSpamMessage(
        sender: String,
        message: String,
        type: String, // Fake Suffix / Honey Trap / Deepfake / Bomber
        isSpam: Boolean = true
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val url = SupabaseConfig.getSupabaseUrl(context).first()
            val anonKey = SupabaseConfig.getSupabaseAnonKey(context).first()

            val msg = SpamMessageDto(
                sender = sender,
                message = message,
                type = type,
                isSpam = isSpam
            )

            val payload = json.encodeToString(msg)
            val endpoint = "$url/rest/v1/spam_messages"

            val code = makeHttpRequest(endpoint, "POST", payload, anonKey)
            if (code in 200..299) Result.success(Unit)
            else Result.failure(Exception("Error saving spam message: $code"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Helper HTTP POST
    private fun makeHttpRequest(endpoint: String, method: String, payload: String, anonKey: String): Int {
        val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            doOutput = true
            connectTimeout = 8000
            readTimeout = 8000
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("apikey", anonKey)
            setRequestProperty("Authorization", "Bearer $anonKey")
            setRequestProperty("Prefer", "return=minimal")
        }

        OutputStreamWriter(conn.outputStream).use { writer ->
            writer.write(payload)
            writer.flush()
        }
        return conn.responseCode
    }

    // Helper HTTP GET
    private fun makeHttpGet(endpoint: String, anonKey: String): Pair<Int, String> {
        val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8000
            readTimeout = 8000
            setRequestProperty("apikey", anonKey)
            setRequestProperty("Authorization", "Bearer $anonKey")
        }

        val code = conn.responseCode
        val response = if (code in 200..299) {
            BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
        } else {
            ""
        }
        return Pair(code, response)
    }
}
